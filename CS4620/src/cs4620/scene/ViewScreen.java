package cs4620.scene;

import java.awt.FileDialog;
import java.io.File;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import blister.GameScreen;
import blister.GameTime;
import blister.ScreenState;
import blister.input.KeyboardEventDispatcher;
import blister.input.KeyboardKeyEventArgs;
import cs4620.common.Scene;
import cs4620.common.SceneObject;
import cs4620.common.event.SceneObjectResourceEvent;
import cs4620.common.event.SceneReloadEvent;
import cs4620.gl.AnimationObject;
import cs4620.gl.CameraController;
import cs4620.gl.GridRenderer;
import cs4620.gl.RenderCamera;
import cs4620.gl.RenderController;
import cs4620.gl.Renderer;
import cs4620.gl.manip.ManipController;
import cs4620.scene.form.RPMaterialData;
import cs4620.scene.form.RPMeshData;
import cs4620.scene.form.RPTextureData;
import cs4620.scene.form.ScenePanel;
import egl.GLError;
import egl.math.Matrix3;
import egl.math.Matrix4;
import egl.math.Vector2;
import egl.math.Vector3;
import ext.csharp.ACEventFunc;
import ext.java.Parser;

public class ViewScreen extends GameScreen {
	Renderer renderer = new Renderer();
	int cameraIndex = 0;
	boolean pick;
	int prevCamScroll = 0;
	boolean wasPickPressedLast = false;
	boolean showGrid = false;
	// changed to false
	
	// Jason: event handler:
	boolean bHappening = false;
	double dStartTime = 0;
	double dNextOccurrence = 0;
	
	// End Jason
	
	
	
	
	SceneApp app;
	ScenePanel sceneTree;
	RPMeshData dataMesh;
	RPMaterialData dataMaterial;
	RPTextureData dataTexture;
	
	RenderController rController;
	CameraController camController;
	ManipController manipController;
	GridRenderer gridRenderer;
	
	@Override
	public int getNext() {
		return getIndex();
	}
	@Override
	protected void setNext(int next) {
	}

	@Override
	public int getPrevious() {
		return -1;
	}
	@Override
	protected void setPrevious(int previous) {
	}

	@Override
	public void build() {
		app = (SceneApp)game;
		
		renderer = new Renderer();
	}
	@Override
	public void destroy(GameTime gameTime) {
	}

	private boolean starTexture = true;
	AnimationObject ao;
	SceneObject so;
	
	
	
	/**
	 * Add Scene Data Hotkeys
	 */
	private final ACEventFunc<KeyboardKeyEventArgs> onKeyPress = new ACEventFunc<KeyboardKeyEventArgs>() {
		@Override
		public void receive(Object sender, KeyboardKeyEventArgs args) {
			switch (args.key) {
			///////////////////
			case Keyboard.KEY_T:
				try {
					ao = new AnimationObject(app.scene);
					so = app.scene.objects.get("Star");
				} catch (Exception e){}
				if (starTexture)ao.changeTexture("Star", "StarMaterial");
				else ao.changeTexture("Star", "NoiseMaterial");
				app.scene.sendEvent(new SceneObjectResourceEvent(so, SceneObjectResourceEvent.Type.Material));
				starTexture = !starTexture;
				break;
			///////////////////
			case Keyboard.KEY_M:
				if(!args.getAlt()) return;
				if(dataMaterial != null) {
					app.otherWindow.tabToForefront("Material");
					dataMaterial.addBasic();
				}
				break;
			case Keyboard.KEY_G:
				showGrid = !showGrid;
				break;
			case Keyboard.KEY_F3:
				FileDialog fd = new FileDialog(app.otherWindow);
				fd.setVisible(true);
				for(File f : fd.getFiles()) {
					String file = f.getAbsolutePath();
					if(file != null) {
						Parser p = new Parser();
						Object o = p.parse(file, Scene.class);
						if(o != null) {
							Scene old = app.scene;
							app.scene = (Scene)o;
							if(old != null) old.sendEvent(new SceneReloadEvent(file));
							return;
						}
					}
				}
				break;
			case Keyboard.KEY_F4:
				try {
					app.scene.saveData("data/scenes/Saved.xml");
				} catch (ParserConfigurationException | TransformerException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public void onEntry(GameTime gameTime) {
		cameraIndex = 0;
		
		rController = new RenderController(app.scene, new Vector2(app.getWidth(), app.getHeight()));
		renderer.buildPasses(rController.env.root);
		camController = new CameraController(app.scene, rController.env, null);
		createCamController();
		manipController = new ManipController(rController.env, app.scene, app.otherWindow);
		gridRenderer = new GridRenderer();
		
		KeyboardEventDispatcher.OnKeyPressed.add(onKeyPress);
		manipController.hook();
		
		Object tab = app.otherWindow.tabs.get("Object");
		if(tab != null) sceneTree = (ScenePanel)tab;
		tab = app.otherWindow.tabs.get("Material");
		if(tab != null) dataMaterial = (RPMaterialData)tab;
		tab = app.otherWindow.tabs.get("Mesh");
		if(tab != null) dataMesh = (RPMeshData)tab;
		tab = app.otherWindow.tabs.get("Texture");
		if(tab != null) dataTexture = (RPTextureData)tab;
		
		wasPickPressedLast = false;
		prevCamScroll = 0;
	}
	@Override
	public void onExit(GameTime gameTime) {
		KeyboardEventDispatcher.OnKeyPressed.remove(onKeyPress);
		rController.dispose();
		manipController.dispose();
	}

	private void createCamController() {
		if(rController.env.cameras.size() > 0) {
			RenderCamera cam = rController.env.cameras.get(cameraIndex);
			camController.camera = cam;
		}
		else {
			camController.camera = null;
		}
	}
	
	@Override
	public void update(GameTime gameTime) {
		pick = false;
		int curCamScroll = 0;
		
		int iNumParticles = 200;
		
		// Jason: -----------------------------
				if (bHappening){
					if (gameTime.total-dStartTime > 10){
						bHappening = false;
						for (int i = 0; i < iNumParticles; i++){
							app.scene.objects.get("particle_"+i).transformation.set(new Matrix4());
						}
					} else {
						float et = (float)gameTime.elapsed;
						//Vector3 v3_speed = new Vector3((float)(-5*Math.cos((gameTime.total-dStartTime)/3*2*Math.PI) + 5), (float)(5*Math.sin((gameTime.total-dStartTime)/3*2*Math.PI)), 0f);
						et = et/3;
						try {
							//app.scene.objects.get("Debris_1").transformation.set(new Matrix4());
							//app.scene.objects.get("Debris_1").addScale(new Vector3(0.1f, 0.1f, 0.1f));
							
							//app.scene.objects.get("Debris_1").addTranslation(v3_speed);
							
							
							for (int i = 0; i < iNumParticles; i++){
								SceneObject sco = app.scene.objects.get("particle_"+i);
								Vector3 v3_speed = sco.v3_speed;
								sco.addTranslation(new Vector3(v3_speed.x*et, v3_speed.y*et, v3_speed.z*et));
							}
							
						} catch (Exception ex){}
					}
				} else {
					float fRand = (float)Math.random();
					if (fRand < 1/60f){
						bHappening = true;
						
						
						Vector3 v3_loc = new Vector3(1,1,1);
						Matrix4.createRotationX((float)((Math.random()-0.5)*2*Math.PI)).mulDir(v3_loc);
						Matrix4.createRotationY((float)((Math.random()-0.5)*2*Math.PI)).mulDir(v3_loc);
						Matrix4.createRotationZ((float)((Math.random()-0.5)*2*Math.PI)).mulDir(v3_loc);
						v3_loc.normalize();
						
						
						float fSpeed_coeff = 10;
						Vector3 v3_speed = new Vector3(v3_loc);
						v3_speed.mul(fSpeed_coeff);
						
						for (int i = 0; i < iNumParticles; i++){
							SceneObject sco = app.scene.objects.get("particle_"+i);
							sco.addTranslation(v3_loc);
							
							Vector3 v3_perp1 = new Vector3(-v3_loc.y, v3_loc.x, 0);
							Vector3 v3_perp2 = new Vector3(-v3_loc.z, 0, v3_loc.x);
							Vector3 v3_perp = new Vector3();
							fRand = (float)Math.random()-0.5f;
							v3_perp1.mul(2*fRand);
							fRand = (float)Math.random()-0.5f;
							v3_perp2.mul(2*fRand);
							if (v3_perp1.equals(new Vector3(0,0,0)) ){
								// in case they are both zero vectors
								v3_perp1.set(0.5f, 0.5f, 0.5f);
							}
							
							v3_perp.add(v3_perp1).add(v3_perp2);
							v3_perp.normalize();
							v3_perp.mul((float)Math.random());
							
							fRand = (float)Math.random();
							sco.v3_speed.set(v3_speed.x+v3_perp.x, v3_speed.y+v3_perp.y, v3_speed.z+v3_perp.z);
							sco.v3_speed.mul(fRand);
						}
						dStartTime = gameTime.total;
					}
				}
				
				
				// End Jason -------------------------
		

		if(Keyboard.isKeyDown(Keyboard.KEY_EQUALS)) curCamScroll++;
		if(Keyboard.isKeyDown(Keyboard.KEY_MINUS)) curCamScroll--;
		if(rController.env.cameras.size() != 0 && curCamScroll != 0 && prevCamScroll != curCamScroll) {
			if(curCamScroll < 0) curCamScroll = rController.env.cameras.size() - 1;
			cameraIndex += curCamScroll;
			cameraIndex %= rController.env.cameras.size();
			createCamController();
		}
		prevCamScroll = curCamScroll;
		
		if(camController.camera != null) {
			// This part is called every time an update event occurs. Jason's bookmark.
			camController.update(gameTime.elapsed);
			manipController.checkMouse(Mouse.getX(), Mouse.getY(), camController.camera);
		}
		
		
		
		if(Mouse.isButtonDown(1) || Mouse.isButtonDown(0) && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
			if(!wasPickPressedLast) pick = true;
			wasPickPressedLast = true;
		}
		else wasPickPressedLast = false;
		
		// View A Different Scene
		if(rController.isNewSceneRequested()) {
			setState(ScreenState.ChangeNext);
		}
	}
	
	@Override
	public void draw(GameTime gameTime) {
		rController.update(renderer, camController, gameTime);

		if(pick && camController.camera != null) {
			manipController.checkPicking(renderer, camController.camera, Mouse.getX(), Mouse.getY());
		}
		
		Vector3 bg = app.scene.background;
		GL11.glClearColor(bg.x, bg.y, bg.z, 0);
		GL11.glClearDepth(1.0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		if(camController.camera != null){
			renderer.draw(camController.camera, rController.env.lights, gameTime);
			manipController.draw(camController.camera);
			if (showGrid)
				gridRenderer.draw(camController.camera);
		}
        GLError.get("draw");
	}
}
