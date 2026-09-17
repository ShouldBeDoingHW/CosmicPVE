param([string]$RepositoryRoot = (Split-Path -Parent $PSScriptRoot))
$ErrorActionPreference = 'Stop'
$models = @(
    @{ Source='blockbench/belts/bandolier/bandolier_belt.bbmodel'; Runtime='bandolier' },
    @{ Source='blockbench/belts/shock_therapy/Shock_Therapy.bbmodel'; Runtime='shock_therapy' }
)
$modelDir=Join-Path $RepositoryRoot 'src/main/resources/assets/cosmicpve/models/item/belt'
$textureDir=Join-Path $RepositoryRoot 'src/main/resources/assets/cosmicpve/textures/item/belt'
[IO.Directory]::CreateDirectory($modelDir)|Out-Null
[IO.Directory]::CreateDirectory($textureDir)|Out-Null

function Rotate-Point($point,$origin,$rotation) {
    $x=[double]$point[0]-$origin[0];$y=[double]$point[1]-$origin[1];$z=[double]$point[2]-$origin[2]
    $rx=[Math]::PI*$rotation[0]/180;$ry=[Math]::PI*$rotation[1]/180;$rz=[Math]::PI*$rotation[2]/180
    $y1=$y*[Math]::Cos($rx)-$z*[Math]::Sin($rx);$z1=$y*[Math]::Sin($rx)+$z*[Math]::Cos($rx);$y=$y1;$z=$z1
    $x1=$x*[Math]::Cos($ry)+$z*[Math]::Sin($ry);$z1=-$x*[Math]::Sin($ry)+$z*[Math]::Cos($ry);$x=$x1;$z=$z1
    $x1=$x*[Math]::Cos($rz)-$y*[Math]::Sin($rz);$y1=$x*[Math]::Sin($rz)+$y*[Math]::Cos($rz)
    @(($x1+$origin[0]),($y1+$origin[1]),($z+$origin[2]))
}
function Runtime-Point($p) {
    # Belt sources are already authored around their own waist-local origin (+Y up,
    # front -Z). Keep the OBJ centered for GUI/ground/hand item contexts. The player
    # layer alone applies the separate twelve-pixel body-origin-to-waist translation.
    @((.5+$p[0]/16.0),(.5-$p[1]/16.0),(.5+$p[2]/16.0))
}
function Add-Face($lines,$points,$uvs,[ref]$vi,[ref]$ti,$width,$height) {
    $indices=@()
    for($i=0;$i -lt $points.Count;$i++) {
        $p=Runtime-Point $points[$i]
        $lines.Add(('v {0:F7} {1:F7} {2:F7}' -f $p[0],$p[1],$p[2]))
        # These are top-down Blockbench UVs, not bottom-up Wavefront UVs. Runtime
        # wrapper models must use flip_v=false or lower palette rows sample the sigil.
        $lines.Add(('vt {0:F7} {1:F7}' -f ($uvs[$i][0]/$width),($uvs[$i][1]/$height)))
        $indices+=('{0}/{1}' -f $vi.Value,$ti.Value);$vi.Value++;$ti.Value++
    }
    $lines.Add('f '+($indices -join ' '))
}
foreach($entry in $models) {
    $json=Get-Content -Raw (Join-Path $RepositoryRoot $entry.Source)|ConvertFrom-Json
    $lines=[Collections.Generic.List[string]]::new();$lines.Add('# Generated from '+$entry.Source+'; do not hand-edit.')
    $lines.Add('mtllib '+$entry.Runtime+'.mtl');$lines.Add('usemtl belt');$vi=1;$ti=1
    foreach($element in $json.elements) {
        $rotation=if($null -eq $element.rotation){@(0,0,0)}else{$element.rotation}
        $origin=if($null -eq $element.origin){@(0,0,0)}else{$element.origin};$a=$element.from;$b=$element.to
        $corners=@(@($a[0],$a[1],$a[2]),@($b[0],$a[1],$a[2]),@($b[0],$b[1],$a[2]),@($a[0],$b[1],$a[2]),@($a[0],$a[1],$b[2]),@($b[0],$a[1],$b[2]),@($b[0],$b[1],$b[2]),@($a[0],$b[1],$b[2]))
        for($i=0;$i -lt 8;$i++){$corners[$i]=Rotate-Point $corners[$i] $origin $rotation}
        $map=@{north=@(1,0,3,2);south=@(4,5,6,7);west=@(0,4,7,3);east=@(5,1,2,6);up=@(3,7,6,2);down=@(0,1,5,4)}
        foreach($name in @('north','south','west','east','up','down')) {
            $face=$element.faces.$name;if($null -eq $face -or $null -eq $face.texture){continue};$u=$face.uv
            $uv=@(@($u[0],$u[3]),@($u[2],$u[3]),@($u[2],$u[1]),@($u[0],$u[1]));$pts=@()
            foreach($index in $map[$name]){$pts+=,$corners[$index]}
            Add-Face $lines $pts $uv ([ref]$vi) ([ref]$ti) $json.resolution.width $json.resolution.height
        }
    }
    [IO.File]::WriteAllLines((Join-Path $modelDir ($entry.Runtime+'.obj')),$lines)
    [IO.File]::WriteAllText((Join-Path $modelDir ($entry.Runtime+'.mtl')),"newmtl belt`r`nKa 1 1 1`r`nKd 1 1 1`r`nmap_Kd #texture0`r`n")
    $source=[string]$json.textures[0].source
    [IO.File]::WriteAllBytes((Join-Path $textureDir ($entry.Runtime+'.png')),[Convert]::FromBase64String($source.Substring($source.IndexOf(',')+1)))
}
