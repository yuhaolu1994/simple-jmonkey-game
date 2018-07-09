/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import java.util.Random;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Howelu
 */
public class Assignment5 extends SimpleApplication
        implements ActionListener, AnimEventListener, PhysicsCollisionListener {

    private BulletAppState bulletAppState;

    private RigidBodyControl outBox_phy1, outBox_phy2,
            outBox_phy3, outBox_phy4, outBox_phy5, outBox_phy6;

    private RigidBodyControl inBox1_phy1, inBox1_phy2,
            inBox1_phy3, inBox1_phy4, inBox1_phy5;

    private RigidBodyControl inBox2_phy1, inBox2_phy2,
            inBox2_phy3, inBox2_phy4, inBox2_phy5;

    private RigidBodyControl inBox3_phy1, inBox3_phy2,
            inBox3_phy3, inBox3_phy4, inBox3_phy5;

    private RigidBodyControl ball_phy;

    private RigidBodyControl blub_phy;

    private RigidBodyControl joint1_phy, joint2_phy, joint3_phy;

    private RigidBodyControl dinosaur_phy;

    private RigidBodyControl grog_phy;

    private Spatial blub;
    private Spatial dinosaur;
    private Spatial grog;

    private final Vector3f upforce = new Vector3f(0, 4, 0);
    private final Vector3f downforce = new Vector3f(0, -4, 0);
    private final Vector3f forwardforce = new Vector3f(0, 0, -10);
    private final Vector3f backforce = new Vector3f(0, 0, 10);
    private final Vector3f rightforce = new Vector3f(10, 0, 0);
    private final Vector3f leftforce = new Vector3f(-10, 0, 0);
    private boolean applyUpForce = false;
    private boolean applyDownForce = false;
    private boolean applyRightForce = false;
    private boolean applyLeftForce = false;

    private boolean applyForwardForce = false;
    private boolean applyBackForce = false;
    private boolean applyDinosaurRightForce = false;
    private boolean applyDinosaurLeftForce = false;
    private HingeJoint hingeJoint1, hingeJoint2, hingeJoint3;

    private AnimChannel channel;
    private AnimControl control;

    private Node blubNode;
    private Node dinosaurNode;
    private Node grogNode;

    private int count1 = 0;
    private int count2 = 0;

    private Nifty nifty;
    private int ballCount = 1;
    Material selectedColor;
    Map<String, ColorRGBA> colorSelections = new HashMap<>();
    List<String> modelSelections = new ArrayList<>();
    List<String> ballSelections = new ArrayList<>();

    private Material dinosaurMaterial;
    private Material grogMaterial;

    private AudioNode collision_ball, collision_ball_floor, shoot;

    //private CharacterControl blubControl;
    public static void main(String[] args) {
        Assignment5 test = new Assignment5();
        test.start();
    }

    @Override
    public void simpleInitApp() {

        colorSelections.put("Red", ColorRGBA.Red);
        colorSelections.put("Blue", ColorRGBA.Blue);
        colorSelections.put("Green", ColorRGBA.Green);

        modelSelections.add("Blub");
        modelSelections.add("Dinosaur");
        modelSelections.add("Grog");

        ballSelections.add("Original");
        ballSelections.add("Five-fold");

        //configure cam to the scene
        flyCam.setEnabled(false);
        cam.setLocation(new Vector3f(0f, 0f, 80f));
        cam.lookAt(new Vector3f(0f, 0f, -20f), Vector3f.UNIT_Y);

        //set up physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().addCollisionListener(this);

        setupKeys();

        initOutsideBox();
        initInsideBox1();
        initInsideBox2();
        initInsideBox3();
        initLight();
        createHingeJoint1();
        createHingeJoint2();
        createHingeJoint3();
        initAudio();
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
                inputManager,
                audioRenderer,
                guiViewPort);
        nifty = niftyDisplay.getNifty();
        HelloNiftySelectController controller = new HelloNiftySelectController(this);
        nifty.fromXml("Interface/hello-nifty-select-gui.xml", "select", controller);
        guiViewPort.addProcessor(niftyDisplay);
        inputManager.setCursorVisible(true);

        // set the ball color
        selectedColor = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        selectedColor.setColor("Color", colorSelections.values().iterator().next());
    }

    public void doneSelecting() {
        nifty.gotoScreen("start");
        inputManager.setCursorVisible(false);
    }

    public void colorSelected(String color) {
        selectedColor.setColor("Color", colorSelections.get(color));
    }

    public void ballSelected(String ballNumber) {
        if (ballNumber.equals("Original")) {
            ballCount = 1;
        } else if (ballNumber.equals("Five-fold")) {
            ballCount = 5;
        }
    }

    public void modelSelected(String model) {

        switch (model) {
            case "Blub":
                initBlub();
                break;
            case "Dinosaur":
                initDinosaur();
                break;
            case "Grog":
                initGrog();
                break;
        }

    }

    public void initBlub() {
        //load model 
        blub = assetManager.loadModel("Models/blub7/blub7.j3o");
        blub.setLocalTranslation(0, 0, 10);
        blub.scale(1.5f, 1.5f, 1.5f);

        blub_phy = new RigidBodyControl(0.1f);
        blub.addControl(blub_phy);
        bulletAppState.getPhysicsSpace().add(blub_phy);

        blubNode = new Node();
        blubNode.attachChild(blub);
        blubNode.setLocalScale(1.5f);
        rootNode.attachChild(blubNode);

        control = blubNode.getChild("blub_quadrangulated")
                .getControl(AnimControl.class);
        control.addListener(this);
        channel = control.createChannel();
    }

    public void initDinosaur() {
        dinosaur = assetManager.loadModel("Models/Models/triceratops/triceratops.j3o");
        dinosaurMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Models/Models/triceratops/triceratops.jpg");
        Texture dinosaurTexure = assetManager.loadTexture(key);
        dinosaurMaterial.setTexture("ColorMap", dinosaurTexure);
        dinosaur.setMaterial(dinosaurMaterial);
        dinosaur.setLocalTranslation(0, -15, 30);
        dinosaur.rotate(-45f, 45f, 45f);
        dinosaur_phy = new RigidBodyControl(0f);
        dinosaur.addControl(dinosaur_phy);
        bulletAppState.getPhysicsSpace().add(dinosaur_phy);
        dinosaurNode = new Node();
        dinosaurNode.attachChild(dinosaur);
        dinosaurNode.setLocalScale(0.2f);
        rootNode.attachChild(dinosaurNode);
    }

    public void initGrog() {
        grog = assetManager.loadModel("Models/Models/grog5k/grog5k.j3o");
        grog.setLocalTranslation(0, -15, 30);
        grogMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        grogMaterial.setColor("Color", new ColorRGBA(1.0f, 0f, 0f, 1.0f));
        grogMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        grog.setMaterial(grogMaterial);

        grog_phy = new RigidBodyControl(0f);
        grog.addControl(grog_phy);
        bulletAppState.getPhysicsSpace().add(grog_phy);
        grogNode = new Node();
        grogNode.attachChild(grog);
        grogNode.setLocalScale(0.1f);
        rootNode.attachChild(grogNode);
    }

    private void initAudio() {
        collision_ball = new AudioNode(assetManager, "Sounds/collision_ball.wav", false);
        collision_ball.setLooping(false);
        collision_ball.setPositional(true);
        rootNode.attachChild(collision_ball);

        collision_ball_floor = new AudioNode(assetManager, "Sounds/collision_ball&floor.WAV", false);
        collision_ball_floor.setLooping(false);
        collision_ball_floor.setPositional(true);
        rootNode.attachChild(collision_ball_floor);

        shoot = new AudioNode(assetManager, "Sounds/shoot.WAV", false);
        shoot.setLooping(false);
        shoot.setPositional(true);
        shoot.setVolume(0.5f);
        rootNode.attachChild(shoot);
    }

    public void makeBall() {
        // create sphere
        Sphere sphere = new Sphere(32, 32, 0.8f, true, false);
        sphere.setTextureMode(Sphere.TextureMode.Projected);
        Geometry ball_geo = new Geometry("ball", sphere);

        // set material
        ball_geo.setMaterial(selectedColor);
        rootNode.attachChild(ball_geo);

        // Position the cannon ball 
        float y = 20f;
        Random random = new Random();
        float x = -20.0f + random.nextFloat() * 40;
        float z = -10.0f + random.nextFloat() * 20;
        ball_geo.setLocalTranslation(x, y, z);

        //Make the ball physcial with a mass > 0.0f         
        ball_phy = new RigidBodyControl(4f);

        //Add physical ball to physics space.         
        ball_geo.addControl(ball_phy);
        bulletAppState.getPhysicsSpace().add(ball_phy);

        // set the ball direction.
        float y1 = -20f;
        float x1 = -20.0f + random.nextFloat() * 40;
        float z1 = -10.0f + random.nextFloat() * 20;
        //float x1 = 0f;
        //float z1 = 0f;
        Vector3f direnction = new Vector3f(x1, y1, z1);
        ball_phy.setLinearVelocity(direnction);
    }

    // create outside box
    public void initOutsideBox() {

        Material brickMat = assetManager.loadMaterial("Materials/brick.j3m");

        Box box1 = new Box(20, 20, 1f);
        Geometry geom1 = new Geometry("Box", box1);
        geom1.setMaterial(brickMat);

        Box box2 = new Box(20, 20, 1f);
        Geometry geom2 = new Geometry("Box", box2);
        geom2.setMaterial(brickMat);

        Box box3 = new Box(1f, 20f, 41f);
        Geometry geom3 = new Geometry("Box", box3);
        geom3.setMaterial(brickMat);

        Box box4 = new Box(1f, 20f, 41f);
        Geometry geom4 = new Geometry("Box", box4);
        geom4.setMaterial(brickMat);

        Box box5 = new Box(20, 1f, 40);
        Geometry geom5 = new Geometry("Box", box5);
        geom5.setMaterial(brickMat);

        Box box6 = new Box(20, 1f, 40);
        Geometry geom6 = new Geometry("Box", box6);
        geom6.setMaterial(brickMat);

        geom1.setLocalTranslation(0, 0, 40);
        geom2.setLocalTranslation(0, 0, -20);
        geom3.setLocalTranslation(21f, 0, 20);
        geom4.setLocalTranslation(-21f, 0, 20);
        geom5.setLocalTranslation(0, -20.5f, 20);
        geom6.setLocalTranslation(0, 20.5f, 20);

        //rootNode.attachChild(geom1);
        rootNode.attachChild(geom2);
        rootNode.attachChild(geom3);
        rootNode.attachChild(geom4);
        rootNode.attachChild(geom5);
        rootNode.attachChild(geom6);
        // make the outbox physical without mass
        outBox_phy1 = new RigidBodyControl(0.0f);
        outBox_phy2 = new RigidBodyControl(0.0f);
        outBox_phy3 = new RigidBodyControl(0.0f);
        outBox_phy4 = new RigidBodyControl(0.0f);
        outBox_phy5 = new RigidBodyControl(0.0f);
        outBox_phy6 = new RigidBodyControl(0.0f);
        geom1.addControl(outBox_phy1);
        geom2.addControl(outBox_phy2);
        geom3.addControl(outBox_phy3);
        geom4.addControl(outBox_phy4);
        geom5.addControl(outBox_phy5);
        geom6.addControl(outBox_phy6);
        bulletAppState.getPhysicsSpace().add(outBox_phy1);
        bulletAppState.getPhysicsSpace().add(outBox_phy2);
        bulletAppState.getPhysicsSpace().add(outBox_phy3);
        bulletAppState.getPhysicsSpace().add(outBox_phy4);
        bulletAppState.getPhysicsSpace().add(outBox_phy5);
        bulletAppState.getPhysicsSpace().add(outBox_phy6);
    }

    // create red tray object
    public void initInsideBox1() {
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", new ColorRGBA(1.0f, 0f, 0f, 0.5f));
        mat2.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Box box1 = new Box(4.1f, 4, 0.1f);
        Geometry geom6 = new Geometry("Box", box1);
        geom6.setMaterial(mat2);

        Box box2 = new Box(4.1f, 4, 0.1f);
        Geometry geom7 = new Geometry("Box", box2);
        geom7.setMaterial(mat2);

        Box box3 = new Box(0.1f, 4f, 4.1f);
        Geometry geom8 = new Geometry("Box", box3);
        geom8.setMaterial(mat2);

        Box box4 = new Box(0.1f, 4f, 4.1f);
        Geometry geom9 = new Geometry("Box", box4);
        geom9.setMaterial(mat2);

        Box box5 = new Box(4, 0.1f, 4);
        Geometry geom10 = new Geometry("BottomBox1", box5);
        geom10.setMaterial(mat2);

        geom6.setLocalTranslation(0, 10, 4);
        geom7.setLocalTranslation(0, 10, -4);
        geom8.setLocalTranslation(4.1f, 10, 0);
        geom9.setLocalTranslation(-4.1f, 10, 0);
        geom10.setLocalTranslation(0, 5.95f, 0);

        // make the inbox1 physical without mass
        inBox1_phy1 = new RigidBodyControl(0.0f);
        inBox1_phy2 = new RigidBodyControl(0.0f);
        inBox1_phy3 = new RigidBodyControl(0.0f);
        inBox1_phy4 = new RigidBodyControl(0.0f);
        inBox1_phy5 = new RigidBodyControl(1.0f);

        geom6.addControl(inBox1_phy1);
        geom7.addControl(inBox1_phy2);
        geom8.addControl(inBox1_phy3);
        geom9.addControl(inBox1_phy4);
        geom10.addControl(inBox1_phy5);

        bulletAppState.getPhysicsSpace().add(inBox1_phy1);
        bulletAppState.getPhysicsSpace().add(inBox1_phy2);
        bulletAppState.getPhysicsSpace().add(inBox1_phy3);
        bulletAppState.getPhysicsSpace().add(inBox1_phy4);
        bulletAppState.getPhysicsSpace().add(inBox1_phy5);

        rootNode.attachChild(geom6);
        rootNode.attachChild(geom7);
        rootNode.attachChild(geom8);
        rootNode.attachChild(geom9);
        rootNode.attachChild(geom10);

    }

    // create green tray object
    public void initInsideBox2() {
        Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", new ColorRGBA(0f, 1.0f, 0f, 0.5f));
        mat3.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Box box1 = new Box(4.1f, 4, 0.1f);
        Geometry geom6 = new Geometry("Box", box1);
        geom6.setMaterial(mat3);

        Box box2 = new Box(4.1f, 4, 0.1f);
        Geometry geom7 = new Geometry("Box", box2);
        geom7.setMaterial(mat3);

        Box box3 = new Box(0.1f, 4f, 4.1f);
        Geometry geom8 = new Geometry("Box", box3);
        geom8.setMaterial(mat3);

        Box box4 = new Box(0.1f, 4f, 4.1f);
        Geometry geom9 = new Geometry("Box", box4);
        geom9.setMaterial(mat3);

        Box box5 = new Box(4, 0.1f, 4);
        Geometry geom10 = new Geometry("BottomBox2", box5);
        geom10.setMaterial(mat3);

        geom6.setLocalTranslation(-10, -10, 4);
        geom7.setLocalTranslation(-10, -10, -4);
        geom8.setLocalTranslation(-6f, -10, 0);
        geom9.setLocalTranslation(-14f, -10, 0);
        geom10.setLocalTranslation(-10, -14f, 0);

        // make the inbox2 physical without mass
        inBox2_phy1 = new RigidBodyControl(0.0f);
        inBox2_phy2 = new RigidBodyControl(0.0f);
        inBox2_phy3 = new RigidBodyControl(0.0f);
        inBox2_phy4 = new RigidBodyControl(0.0f);
        inBox2_phy5 = new RigidBodyControl(1.0f);

        geom6.addControl(inBox2_phy1);
        geom7.addControl(inBox2_phy2);
        geom8.addControl(inBox2_phy3);
        geom9.addControl(inBox2_phy4);
        geom10.addControl(inBox2_phy5);

        bulletAppState.getPhysicsSpace().add(inBox2_phy1);
        bulletAppState.getPhysicsSpace().add(inBox2_phy2);
        bulletAppState.getPhysicsSpace().add(inBox2_phy3);
        bulletAppState.getPhysicsSpace().add(inBox2_phy4);
        bulletAppState.getPhysicsSpace().add(inBox2_phy5);

        rootNode.attachChild(geom6);
        rootNode.attachChild(geom7);
        rootNode.attachChild(geom8);
        rootNode.attachChild(geom9);
        rootNode.attachChild(geom10);
    }

    // create blue tray object
    public void initInsideBox3() {
        Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", new ColorRGBA(0f, 0f, 1.0f, 0.5f));
        mat3.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Box box1 = new Box(4.1f, 4, 0.1f);
        Geometry geom6 = new Geometry("Box", box1);
        geom6.setMaterial(mat3);

        Box box2 = new Box(4.1f, 4, 0.1f);
        Geometry geom7 = new Geometry("Box", box2);
        geom7.setMaterial(mat3);

        Box box3 = new Box(0.1f, 4f, 4.1f);
        Geometry geom8 = new Geometry("Box", box3);
        geom8.setMaterial(mat3);

        Box box4 = new Box(0.1f, 4f, 4.1f);
        Geometry geom9 = new Geometry("Box", box4);
        geom9.setMaterial(mat3);

        Box box5 = new Box(4, 0.1f, 4);
        Geometry geom10 = new Geometry("BottomBox3", box5);
        geom10.setMaterial(mat3);

        geom6.setLocalTranslation(7, -5, 4);
        geom7.setLocalTranslation(7, -5, -4);
        geom8.setLocalTranslation(3f, -5, 0);
        geom9.setLocalTranslation(11f, -5, 0);
        geom10.setLocalTranslation(7, -9f, 0);

        // make the inbox2 physical without mass
        inBox3_phy1 = new RigidBodyControl(0.0f);
        inBox3_phy2 = new RigidBodyControl(0.0f);
        inBox3_phy3 = new RigidBodyControl(0.0f);
        inBox3_phy4 = new RigidBodyControl(0.0f);
        inBox3_phy5 = new RigidBodyControl(1.0f);

        geom6.addControl(inBox3_phy1);
        geom7.addControl(inBox3_phy2);
        geom8.addControl(inBox3_phy3);
        geom9.addControl(inBox3_phy4);
        geom10.addControl(inBox3_phy5);

        bulletAppState.getPhysicsSpace().add(inBox3_phy1);
        bulletAppState.getPhysicsSpace().add(inBox3_phy2);
        bulletAppState.getPhysicsSpace().add(inBox3_phy3);
        bulletAppState.getPhysicsSpace().add(inBox3_phy4);
        bulletAppState.getPhysicsSpace().add(inBox3_phy5);

        rootNode.attachChild(geom6);
        rootNode.attachChild(geom7);
        rootNode.attachChild(geom8);
        rootNode.attachChild(geom9);
        rootNode.attachChild(geom10);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Shoot")) {
            for (int i = 0; i < ballCount; i++) {
                makeBall();
            }
        }
        if (name.equals("Up") && !isPressed) {
            applyUpForce = true;
        } else {
            applyUpForce = false;
        }
        if (name.equals("Down") && !isPressed) {
            applyDownForce = true;
        } else {
            applyDownForce = false;
        }
        if (name.equals("Right") && !isPressed) {
            applyRightForce = true;
        } else {
            applyRightForce = false;
        }
        if (name.equals("Left") && !isPressed) {
            applyLeftForce = true;
        } else {
            applyLeftForce = false;
        }

        if (name.equals("DinosaurForward") && !isPressed) {
            applyForwardForce = true;
        } else {
            applyForwardForce = false;
        }
        if (name.equals("DinosaurBack") && !isPressed) {
            applyBackForce = true;
        } else {
            applyBackForce = false;
        }
        if (name.equals("DinosaurRight") && !isPressed) {
            applyDinosaurRightForce = true;
        } else {
            applyDinosaurRightForce = false;
        }
        if (name.equals("DinosaurLeft") && !isPressed) {
            applyDinosaurLeftForce = true;
        } else {
            applyDinosaurLeftForce = false;
        }

        if (name.equals("animation3") && !isPressed) {
            channel.setAnim("animation3", 0.50f);
            channel.setLoopMode(LoopMode.DontLoop);
        }

        if (name.equals("animation1") && !isPressed) {
            count1++;
            if (count1 % 2 == 1) {
                channel.setAnim("animation1", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
            } else {
                channel.setLoopMode(LoopMode.DontLoop);
            }

        }
        if (name.equals("animation2") && !isPressed) {
            count2++;
            if (count2 % 2 == 1) {
                channel.setAnim("animation2", 0.50f);
                channel.setLoopMode(LoopMode.Loop);
            } else {
                channel.setLoopMode(LoopMode.DontLoop);
            }

        }
    }

    private void setupKeys() {
        inputManager.addMapping("Shoot", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("DinosaurForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("DinosaurBack", new KeyTrigger(KeyInput.KEY_X));
        inputManager.addMapping("DinosaurRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("DinosaurLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("animation3", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addMapping("animation1", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("animation2", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addListener(this, "Shoot", "Up", "Down", "DinosaurForward", "DinosaurBack", "DinosaurRight",
                "DinosaurLeft", "Right", "Left",
                "animation1", "animation2", "animation3");
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (applyUpForce) {

            blub_phy.applyCentralForce(upforce);

        }
        if (applyDownForce) {

            blub_phy.applyCentralForce(downforce);

        }
        if (applyRightForce) {

            blub_phy.applyCentralForce(rightforce);

        }
        if (applyLeftForce) {

            blub_phy.applyCentralForce(leftforce);

        }
        if (applyForwardForce) {

            dinosaur_phy.applyCentralForce(forwardforce);
        }
        if (applyBackForce) {

            dinosaur_phy.applyCentralForce(backforce);
        }
        if (applyDinosaurRightForce) {

            dinosaur_phy.applyCentralForce(rightforce);
        }
        if (applyDinosaurLeftForce) {

            dinosaur_phy.applyCentralForce(leftforce);
        }
    }

    private void initLight() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0f, 0f, 20f));
        rootNode.addLight(sun);
        rootNode.addLight(new AmbientLight());
    }

    private void createHingeJoint1() {
        Geometry joint1 = new Geometry("Cylinder1", new Cylinder(6, 12, 0.05f, 4));
        Material matJoint1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matJoint1.setColor("Color", new ColorRGBA(1.0f, 0f, 0f, 0.3f));
        matJoint1.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        joint1.setMaterial(matJoint1);
        joint1.setLocalTranslation(4f, 5.95f, 0f);
        //rootNode.attachChild(joint1);
        joint1_phy = new RigidBodyControl(0f);
        joint1.addControl(joint1_phy);
        bulletAppState.getPhysicsSpace().add(joint1_phy);
        hingeJoint1 = new HingeJoint(joint1_phy, inBox1_phy5, Vector3f.ZERO,
                new Vector3f(4f, 0, 0f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
        bulletAppState.getPhysicsSpace().add(hingeJoint1);
        hingeJoint1.enableMotor(true, 1f, 10f);
    }

    private void createHingeJoint2() {
        Geometry joint2 = new Geometry("Cylinder2", new Cylinder(6, 12, 0.05f, 4));
        Material matJoint2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matJoint2.setColor("Color", new ColorRGBA(0f, 1.0f, 0f, 0.3f));
        matJoint2.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        joint2.setMaterial(matJoint2);
        joint2.setLocalTranslation(-6, -14f, 0f);
        //rootNode.attachChild(joint2);
        joint2_phy = new RigidBodyControl(0f);
        joint2.addControl(joint2_phy);
        bulletAppState.getPhysicsSpace().add(joint2_phy);
        hingeJoint2 = new HingeJoint(joint2_phy, inBox2_phy5, Vector3f.ZERO,
                new Vector3f(4f, 0, 0f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
        bulletAppState.getPhysicsSpace().add(hingeJoint2);
        hingeJoint2.enableMotor(true, 1f, 10f);
    }

    private void createHingeJoint3() {
        Geometry joint3 = new Geometry("Cylinder2", new Cylinder(6, 12, 0.05f, 4));
        Material matJoint3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matJoint3.setColor("Color", new ColorRGBA(0f, 0f, 1.0f, 0.3f));
        matJoint3.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        joint3.setMaterial(matJoint3);
        joint3.setLocalTranslation(11, -9f, 0f);
        //rootNode.attachChild(joint3);
        joint3_phy = new RigidBodyControl(0f);
        joint3.addControl(joint3_phy);
        bulletAppState.getPhysicsSpace().add(joint3_phy);
        hingeJoint3 = new HingeJoint(joint3_phy, inBox3_phy5, Vector3f.ZERO,
                new Vector3f(4f, 0, 0f), Vector3f.UNIT_Z, Vector3f.UNIT_Z);
        bulletAppState.getPhysicsSpace().add(hingeJoint3);
        hingeJoint3.enableMotor(true, 1f, 10f);
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {

    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {

    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        float a, b, c;
        a = 0.5f;
        b = 0.1f;
        c = 10f;

        if ("ball".equals(event.getNodeA().getName()) && "ball".equals(event.getNodeB().getName())) {
            if (event.getAppliedImpulse() > a) {
                collision_ball.setVolume(event.getAppliedImpulse());
                collision_ball.playInstance();
                collision_ball.setRefDistance(b);
                collision_ball.setMaxDistance(c);

            }
        }
        if ("Box".equals(event.getNodeA().getName()) && "ball".equals(event.getNodeB().getName())) {
            if (event.getAppliedImpulse() > a) {
                collision_ball_floor.setVolume(event.getAppliedImpulse());
                collision_ball_floor.playInstance();
                collision_ball_floor.setRefDistance(b);
                collision_ball_floor.setMaxDistance(c);

            }
        }
    }

}
