var uiParams = [
	{
		"id": "text",
		"displayName": "Text",
		"type": "text",
		"required": false,
		"default": ""
	},
	{
		"id": "textpos0",
		"displayName": "Text Pos1",
		"type": "location",
		"required": false,
		"default": ""
	},
	{
		"id": "textpos1",
		"displayName": "Text Pos2",
		"type": "location",
		"required": false,
		"default": ""
	},
	{
		"id": "image",
		"displayName": "Image",
		"type": "url",
		"required": false
	},
	{
		"id": "imagepos0",
		"displayName": "Image Pos1",
		"type": "location",
		"required": false,
		"default": ""
	},
	{
		"id": "imagepos1",
		"displayName": "Image Pos2",
		"type": "location",
		"required": false,
		"default": ""
	},
	{
		"id": "engraveDepth",
		"displayName": "Engrave Depth",
		"type": "range",
		"rangeMin": -1,
		"rangeMax": 1,
		"step": 0.1,
		"default": 0.5
	}


];

function spheres(radius, count){

	var union = new Union();
	var blend = union.getParam("blend").setValue(0.5*MM);
	var x0 = -radius;
	var dx = 2*radius/(count-1);
	for (i = 0; i < count; i++) {
		union.add(new Sphere(x0 + dx*i, 0, 0, radius));
	}
	return union;
}

function getRotation(from, to){

	var a = new Vector3d();
	a.cross(from, to);
	var sina = a.length();

	var cosa = to.dot(from);
	var angle = Math.atan2(sina, cosa);

	if(Math.abs(sina) < 1.e-8)
		return new AxisAngle4d(1,0,0,0);

	a.normalize();
	return new AxisAngle4d(a.x,a.y,a.z,angle);
}
// calculates transformation of text to be placed between point p0 and p1 wih normals n0 and n1
function getTextTransform(p0, n0, p1, n1){

	// calculation of text transform
	var nn = new Vector3d();
	nn.add(n0,n1);
	nn.normalize();

	var center = new Vector3d();
	center.add(p0, p1);
	center.scale(0.5);
	var xdir = new Vector3d();
	xdir.sub(p1,p0);
	var len = xdir.length();
	xdir.normalize();

	var xaxis = new Vector3d(1,0,0);

	var aa1 = getRotation(xaxis, xdir);

	var m1 = new Matrix3d();
	m1.set(aa1);
	var zaxis = new Vector3d(0,0,1);
	m1.transform(zaxis); // zaxis after first rotation

	var aa2 = getRotation(zaxis, nn);
	var trans = new CompositeTransform();
	trans.add(new Rotation(aa1.x,aa1.y,aa1.z, aa1.angle));
	trans.add(new Rotation(aa2.x,aa2.y,aa2.z, aa2.angle));
	trans.add(new Translation(center));
	return trans;
}

function main(args) {
	var textpos0 = args.textpos0;
	var textpos1 = args.textpos1;
	var text = args.text;
	var textBox = null;
	var imagePath = args.image;
	var imagepos0 = args.imagepos0;
	var imagepos1 = args.imagepos1;
	var imgBox = null;
	var engraveDepth = args.engraveDepth * MM;


	if (text != null && textpos0 && textpos1) {
		var tpos0Str = textpos0.split(",");
		var tpos1Str = textpos1.split(",");

		var tpos0 = [parseFloat(tpos0Str[0]),parseFloat(tpos0Str[1]),parseFloat(tpos0Str[2])];
		var tnormal0 = [parseFloat(tpos0Str[3]),parseFloat(tpos0Str[4]),parseFloat(tpos0Str[5])];
		var tpos1 = [parseFloat(tpos1Str[0]),parseFloat(tpos1Str[1]),parseFloat(tpos1Str[2])];
		var tnormal1 = [parseFloat(tpos1Str[3]),parseFloat(tpos1Str[4]),parseFloat(tpos1Str[5])];

		var tp0 = new Vector3d(tpos0[0],tpos0[1],tpos0[2]);
		var tp1 = new Vector3d(tpos1[0],tpos1[1],tpos1[2]);
		var tn0 = new Vector3d(tnormal0[0],tnormal0[1],tnormal0[2]);
		var tn1 = new Vector3d(tnormal1[0],tnormal1[1],tnormal1[2]);
		tn0.normalize();
		tn1.normalize();

		var tp01 = new Vector3d();
		tp01.sub(tp1,tp0);

		var tbx = tp01.length();
		var tby = 5*MM;
		var tbz = 10*MM;
		var tvs = 0.1*MM;

		textBox = new Text(text, "Times New Roman", tbx, tby, tbz, tvs);

		textBox.getParam("rounding").setValue(0.*MM);
		textBox.getParam("blurWidth").setValue(0.1*MM);
		textBox.setTransform(getTextTransform(tp0,tn0,tp1,tn1));
	}
	if (imagePath && imagepos0 && imagepos1) {
		var image = loadImage(imagePath);
		var pos0Str = imagepos0.split(",");
		var pos1Str = imagepos1.split(",");

		var pos0 = [parseFloat(pos0Str[0]),parseFloat(pos0Str[1]),parseFloat(pos0Str[2])];
		var normal0 = [parseFloat(pos0Str[3]),parseFloat(pos0Str[4]),parseFloat(pos0Str[5])];
		var pos1 = [parseFloat(pos1Str[0]),parseFloat(pos1Str[1]),parseFloat(pos1Str[2])];
		var normal1 = [parseFloat(pos1Str[3]),parseFloat(pos1Str[4]),parseFloat(pos1Str[5])];

		var p0 = new Vector3d(pos0[0],pos0[1],pos0[2]);
		var p1 = new Vector3d(pos1[0],pos1[1],pos1[2]);
		var n0 = new Vector3d(normal0[0],normal0[1],normal0[2]);
		var n1 = new Vector3d(normal1[0],normal1[1],normal1[2]);
		n0.normalize();
		n1.normalize();

		var p01 = new Vector3d();
		p01.sub(p1,p0);

		var bx = p01.length();
		var by = bx * image.getHeight()/image.getWidth();
		var bz = 20*MM;
		var vs = 0.1*MM;

		imgBox = new ImageBitmap(image, bx, by, bz, vs);
		imgBox.setBlurWidth(0.1*MM);
		imgBox.getParam("rounding").setValue(0.0*MM);
		imgBox.setTransform(getTextTransform(p0,n0,p1,n1));
	}

	var radius = 7*MM;
	var num = 3;

	r = radius * num * 0.7;
	var shape = new Union(spheres(radius,3), new Box(0,-r * 0.9,0,r,r,r));

	var bump = null;
	if (textBox !== null){
		if(imgBox === null) {
			bump = textBox;
		} else {
			bump = new Intersection(imgBox, textBox);
		}
	} else { // textBox == null
		if(imgBox !== null) {
			bump = imgBox;
		}
	}

	if(bump === null){
		return new Shape(shape,new Bounds(-r,r,-r,r,-r,r));
	} else {
		var eng = new Engraving(shape, bump);
		eng.getParam("depth").setValue(engraveDepth);
		eng.getParam("blend").setValue(0.2*MM);
		return new Shape(eng,new Bounds(-r,r,-r,r,-r,r));
	}
}
