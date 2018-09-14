#define PI2 6.283184

#define CV 0.1
#define ST 0.05

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D vidtexture;
uniform sampler2D vidtexture_old;

uniform float time;

varying vec4 vertColor;
varying vec4 vertTexCoord;


vec4 colorat(vec2 uv) {
	vec4 O = texture2D(vidtexture, uv) * vertColor;
    float m = min(O.r,min(O.g,O.b)),
          M = max(O.r,max(O.g,O.b));
    

    
    float l = ( .5+.5*cos(time) ) *.95 + .05;
    
    vec4 M1 = textureLod( vidtexture, uv, l*8. ),          
         M2 = textureLod( vidtexture_old, uv, l*8. ),          
         S = sqrt(M2-M1*M1);                            
         
    O =  .5 + .5 * ( texture( vidtexture, uv ) - M1 ) / S; 
    
    O = vec4( length(O.rgb) ) / 1.7; 
    return O;
}

vec4 blur(vec2 uv) {
	vec4 col = vec4(0.0);
	for(float r0 = 0.0; r0 < 1.0; r0 += ST) {
		float r = r0 * CV;
		for(float a0 = 0.0; a0 < 1.0; a0 += ST) {
			float a = a0 * PI2;
			col += colorat(uv + vec2(cos(a), sin(a)) * r);
		}
	}
	col *= ST * ST;
	return col;
}

void main()
{

 	gl_FragColor = blur(vertTexCoord.st); 

  	 // gl_FragColor = mix(O, vec4(length(O.xyz)), smoothstep(.3,.33, (O.g-m)/(M-m)) );
	 // O = vec4(1.-(O.g-m)/(M-m)); // orangeness map   
	 // O = mix(O, vec4(length(O.xyz)), smoothstep(.0,.1, abs((O.g-m)/(M-m))-.27) );
}


