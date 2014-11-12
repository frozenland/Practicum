package cs4620.gl;

import java.util.ArrayList;

import cs4620.common.Material;
import cs4620.common.Mesh;
import cs4620.common.Scene;
import cs4620.common.SceneCamera;
import cs4620.common.SceneObject;
import cs4620.common.Texture;
import cs4620.common.event.SceneCollectionModifiedEvent;
import cs4620.common.event.SceneEvent;
import cs4620.common.event.SceneEventQueue;
import cs4620.common.event.SceneObjectResourceEvent;
import cs4620.common.event.SceneReloadEvent;
import cs4620.common.event.SceneTransformationEvent;
import egl.IDisposable;
import egl.math.Matrix4;
import egl.math.Vector2;
import egl.math.Vector3;

public class RenderController implements IDisposable {
	public final SceneEventQueue queue;
	public final Scene scene;
	public RenderEnvironment env;
	private boolean requestNewScene = false;
	
	public RenderController(Scene s, Vector2 viewSize) {
		scene = s;
		queue = new SceneEventQueue(scene);
		scene.addListener(queue);
		env = RenderTreeBuilder.build(scene, viewSize);
	}
	@Override
	public void dispose() {
		env.dispose();
	}

	public boolean isNewSceneRequested() {
		return requestNewScene;
	}
	
	public void update(Renderer r, CameraController camController) {
		
		ArrayList<SceneEvent> le = new ArrayList<>();
		queue.getEvents(le);
		if(le.size() == 0) return;
		
		boolean isTreeModified = false;
		boolean areTransformsModified = false;
		boolean areResourcesModified = false;
		
		for(SceneEvent e : le) {
			if(e instanceof SceneCollectionModifiedEvent) {
				SceneCollectionModifiedEvent cme = (SceneCollectionModifiedEvent)e;
				switch (cme.dataType) {
				case Texture:
					areResourcesModified = true;
					if(cme.isAdded) {
						Texture t = scene.textures.get(cme.name);
						if(t != null) env.addTexture(t);
					}
					else {
						env.removeTexture(cme.name);
					}
					break;
				case Mesh:
					areResourcesModified = true;
					if(cme.isAdded) {
						Mesh m = scene.meshes.get(cme.name);
						if(m != null) env.addMesh(m);
					}
					else {
						env.removeMesh(cme.name);
					}
					break;
				case Material:
					areResourcesModified = true;
					if(cme.isAdded) {
						Material m = scene.materials.get(cme.name);
						if(m != null) env.addMaterial(m);
					}
					else {
						env.removeMaterial(cme.name);
					}
					break;
				case Object:
					isTreeModified = !cme.isAdded || (scene.objects.get(cme.name) != null);
					break;
				default:
					break;
				}
			}
			else if(e instanceof SceneTransformationEvent) {
				areTransformsModified = true;
			}
			else if(e instanceof SceneObjectResourceEvent) {
				areResourcesModified = true;
			}
			else if(e instanceof SceneReloadEvent) {
				requestNewScene = true;
			}
		}
		
		if(isTreeModified) {
			RenderTreeBuilder.buildTree(scene, env);
			if(camController.camera != null) {
				SceneObject so = camController.camera.sceneObject;
				for(RenderCamera cam : env.cameras ) {
					if(cam.sceneObject == so) {
						camController.camera = cam;
						break;
					}
				}				
			}
			else if(env.cameras.size() > 0) {
				camController.camera = env.cameras.get(0);
			}
		}
		else if(areTransformsModified) {
			RenderTreeBuilder.rippleTransformations(env);
		}
		
		if(areResourcesModified || isTreeModified) {
			env.linkObjectResources();
			r.buildPasses(env.root);
		}
		
		////////////////////////////////

//		for (SceneObject s : scene.objects) {
//			if (!(s instanceof SceneCamera)) s.addRotation(new Vector3((float).25, (float).5, 0.f));
//		}
		
//		try {
//			scene.objects.get("Star").addRotation(new Vector3((float).25, (float).5, 0.f));
//		} catch(Exception e) {}
		
		try {
			AnimationObject ao = new AnimationObject(scene);
			ao.rotateObject("Star",  0.f , (float) .5, (float) .5);
		} catch(Exception e) {}
		
		
		////////////////////////////////
		
		
	}
}