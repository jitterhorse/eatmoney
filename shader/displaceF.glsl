#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

//#define PROCESSING_TEXTURE_SHADER

uniform sampler2D texture;
uniform sampler2D dispT;

uniform float time;
uniform vec2 resolution;
uniform vec2 mouse;
uniform vec2 offset;

varying vec4 vertTexCoord;

void main(void) {
   vec4 dispcol = texture2D(dispT, vertTexCoord.xy);

   
   vec2 newpos = vertTexCoord.xy + (dispcol.xy*offset.xy);
   
   if( newpos.x > 1.) newpos.x =1.;
   else if( newpos.x < 0.) newpos.x =0.;
   if( newpos.y > 1.) newpos.y =1.;
   else if( newpos.y < 0.) newpos.y =0.; 
    
   vec4 col0 = texture2D(texture,newpos);
   
   gl_FragColor = col0 * (1.+(dispcol.x + dispcol.y)) + dispcol * vec4(0.1,0.1,0.1,0.6);;

}