package voronoi_test;


import processing.core.PApplet;

public class Drawer extends PApplet
{
	int width,height;
	VoronoiBuilder builder;
	public static Drawer instance;

	public void settings() 
	{
		instance = this;

		builder = VoronoiBuilder.builderInstance;
		width = VoronoiBuilder.width;
		height = builder.height;
		size(width, height);
	}

	public void setup() 
	{
		surface.setSize(width, height);
		instance = this;
	}
		
	public void draw()
	{
		builder.draw(this);
	}
}
