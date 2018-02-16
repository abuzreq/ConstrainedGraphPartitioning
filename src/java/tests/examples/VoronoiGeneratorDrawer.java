package examples;


import java.util.concurrent.Callable;
import java.util.function.Function;

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

	public static Callable onRight,onLeft;
	public void keyPressed()
	{
		 if (key == CODED) 
		 {
			try
			{
			    if (keyCode == RIGHT)
			    {
			    	onRight.call();
			    }
			    else if (keyCode == LEFT)
			    {
			    	onLeft.call();
			    }
			}
			catch(Exception e)
			{
				System.out.println("");
			}
		 }

	}
}
