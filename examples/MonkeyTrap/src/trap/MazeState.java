/*
 * $Id$
 *
 * Copyright (c) 2013 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package trap;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.TestFunction;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.BaseAppState;
import java.nio.ByteBuffer;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class MazeState extends BaseAppState {

    private Node mazeRoot;
    private Maze maze;

    private ImageRaster fogOfWar;
    
    private ColorRGBA visited = new ColorRGBA(0,0,0,0);

    public MazeState( Maze maze ) {
        this.maze = maze;
    }
    
    public Maze getMaze() {
        return maze;
    }

    public void setVisited( int x, int y ) {
        fogOfWar.setPixel(x,y,visited);
    }

    protected void generateMazeGeometry() {
        float mazeScale = 2;
                

        GuiGlobals globals = GuiGlobals.getInstance();
        
        Mesh mesh = MeshGenerator.generateMesh(maze, mazeScale, mazeScale);
        Geometry geom = new Geometry("maze", mesh);
        Texture tex = globals.loadTexture("Textures/trap-atlas.png", true, true );
        Material mat = globals.createMaterial(tex, true).getMaterial();
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.setFloat("AlphaDiscardThreshold", 0.9f);
        geom.setMaterial(mat);
        geom.move(-mazeScale * 0.5f, 0, -mazeScale * 0.5f);

        mazeRoot.attachChild(geom);
        
        Mesh aoMesh = MeshGenerator.generateAmbientOcclusion(maze, mazeScale, mazeScale);
        geom = new Geometry("maze", aoMesh);
        tex = globals.loadTexture("Textures/ao-halo-alpha.png", true, true );
        mat = globals.createMaterial(tex, false).getMaterial();
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthFunc(TestFunction.Equal);
        //mat.setFloat("AlphaDiscardThreshold", 0.1f);
        mat.setColor("Color", ColorRGBA.Black);
        geom.setMaterial(mat);
        geom.move(-mazeScale * 0.5f, 0, -mazeScale * 0.5f);
        geom.setQueueBucket(Bucket.Transparent);

        mazeRoot.attachChild(geom);
 
        // Create the overlay texture we will use for "fog of war"
        int dataSize = maze.getWidth() * maze.getHeight() * 4; 
        ByteBuffer data = BufferUtils.createByteBuffer(dataSize);
        Image img = new Image(Format.ABGR8, maze.getWidth(), maze.getHeight(), data);
        fogOfWar = ImageRaster.create(img);
        tex = new Texture2D(img);
        for( int i = 0; i < maze.getWidth(); i++ ) {
            for( int j = 0; j < maze.getHeight(); j++ ) {
                fogOfWar.setPixel(i, j, ColorRGBA.Black);
            }
        }
 
        Mesh overlayMesh = MeshGenerator.generateOverlay( maze, mazeScale, mazeScale );
        geom = new Geometry("overlay", overlayMesh);
        mat = globals.createMaterial(tex, false).getMaterial();
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.getAdditionalRenderState().setDepthFunc(TestFunction.Equal);
        geom.setMaterial(mat);
        geom.move(-mazeScale * 0.5f, 0, -mazeScale * 0.5f);
        geom.setQueueBucket(Bucket.Transparent);
        mazeRoot.attachChild(geom);
                
    }

    @Override
    protected void initialize( Application app ) {
        mazeRoot = new Node("mazeRoot");        
        generateMazeGeometry();                      
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        Node rootNode = ((SimpleApplication)getApplication()).getRootNode(); 
        rootNode.attachChild(mazeRoot);                                             
    }

    @Override
    protected void disable() {
        mazeRoot.removeFromParent();
    }
}
