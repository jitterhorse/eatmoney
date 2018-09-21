#define PROCESSING_TEXTURE_SHADER

uniform float iTime;
uniform sampler2D texture;
uniform float black;

varying vec4 vertTexCoord;

void main()
{
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = vertTexCoord.st; //fragCoord/iResolution.xy;

    // 4ime varying pixel color
    vec4 col = texture(texture,uv.xy);

    float shiftx1 = (sin(iTime * 0.2)*0.03)+0.05;
    float shiftx2 = (cos(iTime * 0.3)*0.03)+0.05;
    
    float y = smoothstep(0.0,shiftx1,uv.x);
    float z = smoothstep(1.,1.-shiftx2,uv.x);
    float w = min(y,z);
    
    // Output to screen
    gl_FragColor =  col * w * black;
}