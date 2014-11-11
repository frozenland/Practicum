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
	 * @param String mesh : the mesh of the object
	 * @param String material : the material of the object
	 * @param Float rotX : rotation about the x axis
	 * @param Float rotY : rotation about the y axis
	 * @param Float rotZ : rotation about the z axis
	 * */
	public void rotateObject(String mesh, String material, float rotX, float rotY, float rotZ) {
		for (SceneObject s : this.scene.objects) {
			if (s.mesh.equals(mesh) && s.material.equals(material)) {
				s.addRotation(new Vector3(rotX, rotY, rotZ));
				return;
			}
		}
	}
	
}
