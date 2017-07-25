package voronoi_test;


import processing.core.PApplet;

public class VoronoiGeneratorDrawer extends PApplet
{
	int width,height;
	public static VoronoiGeneratorDrawer instance;
    
	public void settings() 
	{
		instance = this;

		width = VoronoiGenerator.instance.width;
		height = VoronoiGenerator.instance.height;
		size(width, height);
	}

	public void setup() 
	{
		surface.setSize(width, height);
		instance = this;
	}
		
	public void draw()
	{
		VoronoiGenerator.instance.draw(this);
	}
}
