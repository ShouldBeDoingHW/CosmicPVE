param([string]$RepositoryRoot = (Split-Path -Parent $PSScriptRoot))
$ErrorActionPreference = 'Stop'

$models = @(
    @{ Source='blockbench/amulets/blood_diamond/blood_diamond.bbmodel'; Runtime='blood_diamond' },
    @{ Source='blockbench/amulets/icicle/icicle.bbmodel'; Runtime='icicle' },
    @{ Source='blockbench/amulets/blackened_heart/blackened_heart.bbmodel'; Runtime='black_heart' },
    @{ Source='blockbench/amulets/Luau_Lei/Luau_Lei.bbmodel'; Runtime='luau_lei' }
)
$modelDir = Join-Path $RepositoryRoot 'src/main/resources/assets/cosmicpve/models/item/amulet'
$textureDir = Join-Path $RepositoryRoot 'src/main/resources/assets/cosmicpve/textures/item/amulet'
[IO.Directory]::CreateDirectory($modelDir) | Out-Null
[IO.Directory]::CreateDirectory($textureDir) | Out-Null

function Rotate-Point($point, $origin, $rotation) {
    $x=[double]$point[0]-[double]$origin[0]; $y=[double]$point[1]-[double]$origin[1]; $z=[double]$point[2]-[double]$origin[2]
    $rx=[Math]::PI*[double]$rotation[0]/180; $ry=[Math]::PI*[double]$rotation[1]/180; $rz=[Math]::PI*[double]$rotation[2]/180
    $y1=$y*[Math]::Cos($rx)-$z*[Math]::Sin($rx); $z1=$y*[Math]::Sin($rx)+$z*[Math]::Cos($rx); $y=$y1; $z=$z1
    $x1=$x*[Math]::Cos($ry)+$z*[Math]::Sin($ry); $z1=-$x*[Math]::Sin($ry)+$z*[Math]::Cos($ry); $x=$x1; $z=$z1
    $x1=$x*[Math]::Cos($rz)-$y*[Math]::Sin($rz); $y1=$x*[Math]::Sin($rz)+$y*[Math]::Cos($rz); $x=$x1; $y=$y1
    return @(($x+[double]$origin[0]),($y+[double]$origin[1]),($z+[double]$origin[2]))
}
function Runtime-Point($point) {
    # Center each item around its bounds. Worn placement belongs to FIXED's
    # translation, not the geometry shared by inventory/chest/hand rendering.
    return @((0.5+([double]$point[0]-$script:modelCenter[0])/16.0),
            (0.5-([double]$point[1]-$script:modelCenter[1])/16.0),
            (0.5+([double]$point[2]-$script:modelCenter[2])/16.0))
}
function Add-Face([Collections.Generic.List[string]]$lines, $points, $uvs, [ref]$vIndex, [ref]$tIndex) {
    $indices=@()
    for($i=0;$i -lt $points.Count;$i++) {
        $p=Runtime-Point $points[$i]
        $lines.Add(('v {0:F7} {1:F7} {2:F7}' -f $p[0],$p[1],$p[2]))
        $lines.Add(('vt {0:F7} {1:F7}' -f ([double]$uvs[$i][0]/$script:textureWidth),([double]$uvs[$i][1]/$script:textureHeight)))
        $indices += ('{0}/{1}' -f $vIndex.Value,$tIndex.Value)
        $vIndex.Value++; $tIndex.Value++
    }
    $lines.Add('f ' + ($indices -join ' '))
}

foreach($entry in $models) {
    $json=Get-Content -Raw (Join-Path $RepositoryRoot $entry.Source) | ConvertFrom-Json
    $script:textureWidth=[double]$json.resolution.width
    $script:textureHeight=[double]$json.resolution.height
    $allPoints=@()
    foreach($element in $json.elements) {
        $rotation=if($null -eq $element.rotation){@(0,0,0)}else{$element.rotation}
        $origin=if($null -eq $element.origin){@(0,0,0)}else{$element.origin}
        if($element.type -eq 'cube') {
            foreach($x in @($element.from[0],$element.to[0])) { foreach($y in @($element.from[1],$element.to[1])) {
                foreach($z in @($element.from[2],$element.to[2])) { $allPoints+=,(Rotate-Point @($x,$y,$z) $origin $rotation) }
            } }
        } else { foreach($vertex in $element.vertices.PSObject.Properties) { $allPoints+=,(Rotate-Point $vertex.Value $origin $rotation) } }
    }
    $script:modelCenter=@(0,0,0)
    for($axis=0;$axis -lt 3;$axis++) {
        $bounds=$allPoints | ForEach-Object { $_[$axis] } | Measure-Object -Minimum -Maximum
        $script:modelCenter[$axis]=($bounds.Minimum+$bounds.Maximum)/2
    }
    Write-Output ($entry.Runtime+' center: '+($script:modelCenter -join ', '))
    $lines=[Collections.Generic.List[string]]::new()
    $lines.Add('# Generated from ' + $entry.Source + '; do not hand-edit.')
    $lines.Add('mtllib ' + $entry.Runtime + '.mtl'); $lines.Add('usemtl amulet')
    $vi=1; $ti=1
    foreach($element in $json.elements) {
        $rotation = if($null -eq $element.rotation) {@(0,0,0)} else {$element.rotation}
        $origin = if($null -eq $element.origin) {@(0,0,0)} else {$element.origin}
        if($element.type -eq 'cube') {
            $a=$element.from; $b=$element.to
            $corners=@(
                @($a[0],$a[1],$a[2]), @($b[0],$a[1],$a[2]), @($b[0],$b[1],$a[2]), @($a[0],$b[1],$a[2]),
                @($a[0],$a[1],$b[2]), @($b[0],$a[1],$b[2]), @($b[0],$b[1],$b[2]), @($a[0],$b[1],$b[2]))
            for($i=0;$i -lt $corners.Count;$i++) {$corners[$i]=Rotate-Point $corners[$i] $origin $rotation}
            $faceCorners=@{north=@(1,0,3,2);south=@(4,5,6,7);west=@(0,4,7,3);east=@(5,1,2,6);up=@(3,7,6,2);down=@(0,1,5,4)}
            foreach($name in @('north','south','west','east','up','down')) {
                $face=$element.faces.$name; if($null -eq $face -or $null -eq $face.texture) {continue}
                $u=$face.uv; $uv=@(@($u[0],$u[3]),@($u[2],$u[3]),@($u[2],$u[1]),@($u[0],$u[1]))
                $pts=@(); foreach($index in $faceCorners[$name]) {$pts += ,$corners[$index]}
                Add-Face $lines $pts $uv ([ref]$vi) ([ref]$ti)
            }
        } elseif($element.type -eq 'mesh') {
            foreach($faceProperty in $element.faces.PSObject.Properties) {
                $face=$faceProperty.Value; if($null -eq $face.texture) {continue}
                $pts=@(); $uv=@()
                foreach($vertexName in $face.vertices) {
                    $pts += ,(Rotate-Point $element.vertices.$vertexName $origin $rotation)
                    $uvValue=$face.uv.$vertexName; $uv += ,@($uvValue[0],$uvValue[1])
                }
                Add-Face $lines $pts $uv ([ref]$vi) ([ref]$ti)
            }
        }
    }
    [IO.File]::WriteAllLines((Join-Path $modelDir ($entry.Runtime+'.obj')),$lines)
    [IO.File]::WriteAllText((Join-Path $modelDir ($entry.Runtime+'.mtl')),"newmtl amulet`r`nKa 1 1 1`r`nKd 1 1 1`r`nmap_Kd #texture0`r`n")
    $source=[string]$json.textures[0].source
    [IO.File]::WriteAllBytes((Join-Path $textureDir ($entry.Runtime+'.png')),[Convert]::FromBase64String($source.Substring($source.IndexOf(',')+1)))
}
