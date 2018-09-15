uniform float iTime;
uniform vec2 resolution;
uniform sampler2D texture;
uniform float thresh;

float noiseish(vec2 coord, vec2 coordMultiplier1, vec2 coordMultiplier2, vec2 coordMultiplier3, vec3 timeMultipliers) {
    return 0.333 * (sin(dot(coordMultiplier1, coord) + timeMultipliers.x * iTime) + sin(dot(coordMultiplier2, coord) + timeMultipliers.y * iTime) + sin(dot(coordMultiplier3, coord) + timeMultipliers.z * iTime));
}

float hash( in vec2 p ) 
{
    return fract(sin(p.x*15.32+p.y*35.78) * 43758.23);
}

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main()
{
	vec2 uv = gl_FragCoord.xy / resolution.y;
    vec2 uvOffset;
    //uvOffset.x = .2 * sin(iTime * 0.41 + 0.7) *pow(abs(uv.y - 0.5), 3.1) - sin(iTime * 0.07 + 0.1);
    //uvOffset.y = -iTime * 0.03 + .05 * sin(iTime * 0.3) * pow(abs(uv.x - 0.5), 1.8);
    uv += uvOffset;
    const float cellResolution = 13.0;
    const float lineSmoothingWidth = 0.15;
    vec2 localUV = fract(uv * cellResolution);
    vec2 cellCoord = floor(uv * cellResolution);
    
    cellCoord.x += 1. + floor(iTime * 0.3 * (cellCoord.y * 0.1) + 0.33);
    cellCoord.y += 1. + floor(iTime * 0.3 * (cellCoord.x * 0.1) + 0.33);
    
    vec2 angle = (hash(cellCoord)*110.) * normalize(vec2(noiseish(cellCoord, vec2(1.7, 0.19), vec2(2.6, 1.1), vec2(0.0), vec3(0.55, 0.93, 0.0)), noiseish(cellCoord, vec2(0.6, 1.9), vec2(1.3, 0.3), vec2(0.0), vec3(1.25, 0.83, 0.0))));
    
    float v = smoothstep(-lineSmoothingWidth, lineSmoothingWidth, abs(fract(dot(localUV, angle) + 0.06*iTime)-0.5) - 0.25);
    
    float borderSmoothingWidth = 0.02 + hash(cellCoord);
    // apply borders
    vec2 centeredLocalUV = localUV - vec2(0.5);
    const float borderDistance = 0.45; // 0.5 = all the way to the edge of the cell
    v = max(v, max(smoothstep(-borderSmoothingWidth, borderSmoothingWidth, abs(centeredLocalUV.x) - borderDistance), smoothstep(-borderSmoothingWidth, borderSmoothingWidth, abs(centeredLocalUV.y) - borderDistance)));
    v*=0.8;

    float alpha = thresh;
    gl_FragColor = vec4(v, v, v, alpha);
}