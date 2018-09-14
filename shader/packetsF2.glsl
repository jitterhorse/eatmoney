
#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;

uniform vec2 texOffset;


varying vec4 vertColor;
varying vec4 backVertColor;
varying vec4 vertTexCoord;

void main() {
  
  
  
  //gl_FragColor = texture2D(texture, vertTexCoord.st) * (gl_FrontFacing ? vertColor : backVertColor);
  
  vec4 distance = mix(vec4(1,1,1,1),vec4(1,1,1,0),smoothstep(0.1,4000.0,gl_FragCoord.z/gl_FragCoord.w));
  gl_FragColor = (gl_FrontFacing ? vertColor : backVertColor)*distance;
  
}