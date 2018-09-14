uniform vec3 count;
uniform float scale;
uniform vec3 textMapDim;

uniform mat4 transform;
uniform mat4 texMatrix;
uniform float aspectT;

attribute vec4 position;
attribute vec4 color;
attribute vec2 texCoord;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {

  float aspect = count.x/count.y;
  float idVert = gl_VertexID;

  vec4 nPosition = position;
  if(idVert == 1 || idVert == 2)  nPosition.x += count.x * scale;
  if(idVert == 2 || idVert == 3)  nPosition.y += count.y * (scale/aspectT);
  
  
  gl_Position = transform * nPosition;

  vertColor = color;
  vertTexCoord = vec4(texCoord.s,texCoord.t,1.0,1.0);
  
  //texMatrix * vec4(texCoord, 1.0, 1.0);
}