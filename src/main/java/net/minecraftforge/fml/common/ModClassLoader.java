/*
 * Forge Mod Loader
 * Copyright (c) 2012-2013 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     cpw - implementation
 */

package net.minecraftforge.fml.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.transformers.ModAPITransformer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableList;

/**
 * A simple delegating class loader used to load mods into the system
 *
 *
 * @author cpw
 *
 */
public class ModClassLoader extends URLClassLoader
{
    private static final List<String> STANDARD_LIBRARIES = ImmutableList.of("jinput.jar", "lwjgl.jar", "lwjgl_util.jar", "rt.jar");
    private LaunchClassLoader mainClassLoader;

    public ModClassLoader(ClassLoader parent) {
        super(new URL[0], null);
        this.mainClassLoader = (LaunchClassLoader)parent;
    }

    public void addFile(File modFile) throws MalformedURLException
    {
        URL url = modFile.toURI().toURL();
        mainClassLoader.addURL(url);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        return mainClassLoader.loadClass(name);
    }

    public File[] getParentSources() {
        try
        {
            List<File> files=new ArrayList<File>();
            for(URL url : mainClassLoader.getSources())
            {
                URI uri = url.toURI();
                if(uri.getScheme().equals("file"))
                {
                    files.add(new File(uri));
                }
            }
            return files.toArray(new File[]{});
        }
        catch (URISyntaxException e)
        {
            FMLLog.log(Level.ERROR, e, "Unable to process our input to locate the minecraft code");
            throw new LoaderException(e);
        }
    }

    public List<String> getDefaultLibraries()
    {
        return STANDARD_LIBRARIES;
    }

    public void clearNegativeCacheFor(Set<String> classList)
    {
        mainClassLoader.clearNegativeEntries(classList);
    }

    public ModAPITransformer addModAPITransformer(ASMDataTable dataTable)
    {
        mainClassLoader.registerTransformer("net.minecraftforge.fml.common.asm.transformers.ModAPITransformer");
        List<IClassTransformer> transformers = mainClassLoader.getTransformers();
        ModAPITransformer modAPI = (ModAPITransformer) transformers.get(transformers.size()-1);
        modAPI.initTable(dataTable);
        return modAPI;
    }
}
