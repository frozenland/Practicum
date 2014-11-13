package cs4620.gl;

import java.util.ArrayList;

import cs4620.common.Mesh;
import cs4620.common.Scene;
import cs4620.common.SceneObject;
import egl.math.Matrix4;
import egl.math.Vector3;

public class AnimationObject{
	public final Scene scene;

	public AnimationObject(Scene scene) {
		this.scene = scene;
	}
	
	
	/**Rotates an object given that objects mesh and material
	 * @param String name : the name of the object
	 * @param Float rotX : rotation about the x axis
	 * @param Float rotY : rotation about the y axis
	 * @param Float rotZ : rotation about the z axis
	 * */
	public void rotateObject(String name, float rotX, float rotY, float rotZ) {
		scene.objects.get(name).addRotation(new Vector3(rotX, rotY, rotZ));
	}
	
	public void changeTexture(String name, String newTexture) {
		scene.objects.get(name).setMaterial(newTexture);
	}
	
}
