#define PROCESSING_LINE_SHADER

varying vec4 vertColor;


uniform vec2 texCoord;



void main(){
    gl_FragColor = vertColor;  
    vec4 distance = mix(vec4(1,1,1,1),vec4(0,0,0,0),smoothstep(0.1,10000.0,gl_FragCoord.z/gl_FragCoord.w));
	gl_FragColor = vertColor * distance;
}