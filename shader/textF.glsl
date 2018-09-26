#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D glyphMap;
uniform sampler2D inputText;


uniform vec3 textMapDim; 	// dims of textMap
uniform vec3 count; 		// dims of requested Text
uniform float initial;		// first glyph in map = 33
uniform float aspectT;

uniform float alpha;

varying vec4 vertColor;
varying vec4 vertTexCoord;

void main() {
  float aspect = aspectT;

  /////////////////////////////////////
  //calc delta for glyphMap
  
  float dx = 1./count.x;
  float dy = 1./count.y;
  
  float posx = (floor((vertTexCoord.s) * count.x)*dx)+0.000001;
  float posy = (floor((1.-vertTexCoord.t) * count.y)*dy)+0.000001;
  
  
  
  /////////////////////////////////////
  //get requested glyph from Text input
 
  float glyph = texture2D(inputText, vec2(posx,posy)).r*255.0;
  if(glyph == 0) glyph = 150;
  if(glyph > initial) glyph -= initial;
     

  
  /////////////////////////////////////
  //calculate position in textMap
  float dxTM = 1./ textMapDim.x;
  float dyTM = (1./ textMapDim.y);
  
  float row = floor(glyph / textMapDim.x);
  float col = glyph - floor(row * textMapDim.x);
  

  float offsetx = col * dxTM;
  float offsety = row * (dyTM);
 
  
 
  float uvx = (fract(vertTexCoord.s * count.x));
  float uvy = (fract(vertTexCoord.t * count.y));
  

  
  gl_FragColor = texture2D(glyphMap,vec2(offsetx + (uvx*dxTM),(1.-offsety)-(uvy*dyTM)));
  gl_FragColor.a *= alpha;

  ////////////////
  //gl_FragColor = vec4(offsetx + (uvx*dx),offsety+(uvy*dy),0,1);
  ///////////////
  
  
  
  //gl_FragColor = vec4(offsetx + (uvx*dx),offsety + (uvy*dy),0,1);
  //gl_FragColor = vec4(posx,0,0,1);
  
  //gl_FragColor = texture2D(glyphMap, vertTexCoord.st) * vertColor;

  
}