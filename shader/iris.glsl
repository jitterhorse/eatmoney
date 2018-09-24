#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform vec3 resolution;

uniform sampler2D it0;
uniform sampler2D it1;
uniform sampler2D it2;
uniform sampler2D it3;
uniform sampler2D it4;

uniform float h0;
uniform float h1;
uniform float h2;
uniform float h3;
uniform float h4;

vec3 blendAverage(vec3 base, vec3 blend) {
	return (base+blend)/1.5;
}

vec3 blendAverage(vec3 base, vec3 blend, float opacity) {
	return (blendAverage(base, blend) * opacity + base * (1.0 - opacity));
}

void main()
{
	vec2 uv = gl_FragCoord.xy / resolution.xy;

	float alpha = 0.;
	vec3 colF = vec3(1,1,1);
	
	vec4 col0 = texture(it0,uv);
	if(uv.y < 1.- h0) col0 = vec4(0,0,0,0);
	alpha = col0.a;
	float line =smoothstep( (1.-h0)-0.008, 1.-h0, uv.y) - smoothstep( 1.-h0, (1.-h0)+0.008, uv.y);
	
	colF = blendAverage(colF,col0.rgb);
	
	vec4 col1 = texture(it1,uv);
	if(uv.y < 1.- h1) col1 = vec4(0,0,0,0);
	alpha = max(alpha,col1.a);
	line += smoothstep( (1.-h1)-0.008, 1.-h1, uv.y) - smoothstep( 1.-h1, (1.-h1)+0.008, uv.y);
	
	//colF = abs(colF - col1.rgb);
	colF = blendAverage(colF,col1.rgb);
	
	vec4 col2 = texture(it2,uv);
	if(uv.y < 1.- h2) col2 = vec4(0,0,0,0);
	alpha = max(alpha,col2.a);
	line += smoothstep( (1.-h2)-0.008, 1.-h2, uv.y) - smoothstep( 1.-h2, (1.-h2)+0.008, uv.y);
	
	//colF = abs(colF - col2.rgb);
	colF = blendAverage(colF,col2.rgb);
	
	vec4 col3 = texture(it3,uv);
	if(uv.y < 1.- h3) col3 = vec4(0,0,0,0);
	alpha = max(alpha,col3.a);
	line += smoothstep( (1.-h3)-0.008, 1.-h3, uv.y) - smoothstep( 1.-h3, (1.-h3)+0.008, uv.y);
	
	//colF = abs(colF -col3.rgb);
	colF = blendAverage(colF,col3.rgb);
	
	vec4 col4 = texture(it4,uv);
	if(uv.y < 1.- h4) col4 = vec4(0,0,0,0);
	alpha = max(alpha,col4.a);
	line += smoothstep( (1.-h4)-0.008, 1.-h4, uv.y) - smoothstep( 1.-h4, (1.-h4)+0.008, uv.y);
	
	//colF = abs(colF -col4.rgb);
	colF = blendAverage(colF,col4.rgb);
	
	
	
	gl_FragColor = vec4(colF,alpha);
	gl_FragColor.r += line;
	
}