#define PI2 6.283184

#define CV 0.1
#define ST 0.05

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D vidtexture;

uniform sampler2D tracktexture;
uniform sampler2D vidtexture_old;

uniform float blurmix;

uniform float time;

varying vec4 vertColor;
varying vec4 vertTexCoord;


vec4 colorat(vec2 uv) {
	vec4 O = texture2D(vidtexture, uv) * vertColor;
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
 	gl_FragColor = mix(texture2D(vidtexture,vertTexCoord.st),blur(vertTexCoord.st),blurmix); 
 	gl_FragColor += texture2D(tracktexture,vec2(vertTexCoord.s,1.-vertTexCoord.t));
}


