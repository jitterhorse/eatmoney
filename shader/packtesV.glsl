const int numLight = 8;

uniform mat4 modelview;
uniform mat4 transform;
uniform mat3 normalMatrix;

uniform vec4 lightPosition[8];
uniform vec3 lightNormal;

attribute vec4 position;
attribute vec4 color;
attribute vec3 normal;

varying vec4 vertColor;
varying vec3 ecNormal;
varying vec3 lightDir[8];

varying vec3 ecVertex;

vec3 rotate(vec3 p,float angle,vec3 rotationOrigin){
    p -= rotationOrigin;
    p *= mat3(cos(angle),-sin(angle),sin(angle),sin(angle),cos(angle),-sin(angle),-sin(angle),-cos(angle),cos(angle));
    p += rotationOrigin;
	return p;
}

mat4 tPos(vec4 translation){
     return mat4(vec4(1.0,0.0,0.0,translation.x),
                 vec4(0.0,1.0,0.0,translation.y),
                 vec4(0.0,0.0,1.0,translation.z),
                 vec4(0.0,0.0,0.0,1.0));
}

void main() {
  float idVert = gl_VertexID;
  float idObj = floor(idVert / 4.0);

  vec4 nposition = (position) * tPos(vec4(0,1*idObj,0,1));
  ecVertex = vec3(modelview *nposition); 	
  gl_Position = transform * nposition;
  vec3 ecPosition = vec3(modelview * nposition);

  ecNormal = normalize(normalMatrix * normal);
  
   for(int i = 0 ;i < numLight ;i++) { 
     lightDir[i] = normalize(lightPosition[i].xyz - ecVertex);
  }
  vertColor = nposition;
 // vertColor = color;
}