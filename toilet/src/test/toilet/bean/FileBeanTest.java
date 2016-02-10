/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package toilet.bean;

import libWebsiteTools.file.FileRepo;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.embeddable.EJBContainer;
import libWebsiteTools.file.Fileupload;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author alpha
 */
public class FileBeanTest {
    private static EJBContainer contain;
    
    public FileBeanTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
//        Map<String, Object> properties = new HashMap<String, Object>();
//        properties.put(EJBContainer.MODULES, new File("build/jar"));
        contain = EJBContainer.createEJBContainer();//properties);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        contain.close();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getFile method, of class FileBean.
     */
    @Test
    public void testGetFile_String() throws Exception {
        System.out.println("getFile");
        String name = "";
        FileRepo instance = (FileRepo)contain.getContext().lookup("java:global/classes/FileBean");
        Fileupload expResult = null;
        Fileupload result = instance.getFile(name);
//        assertEquals(expResult, result);
    }

    /**
     * Test of putFile method, of class FileBean.
     */
    @Test
    public void testPutFile() throws Exception {
        System.out.println("putFile");
        Fileupload upload = null;
        EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
        FileRepo instance = (FileRepo)container.getContext().lookup("java:global/classes/FileBean");
        instance.addFiles(Arrays.asList(upload));
    }

    /**
     * Test of deleteFile method, of class FileBean.
     */
    @Test
    public void testDeleteFile() throws Exception {
        System.out.println("deleteFile");
        Integer fileUploadId = null;
        EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
        FileRepo instance = (FileRepo)container.getContext().lookup("java:global/classes/FileBean");
        instance.deleteFile(fileUploadId);
    }

    /**
     * Test of getAllUploads method, of class FileBean.
     */
    @Test
    public void testGetAllUploads() throws Exception {
        System.out.println("getAllUploads");
        EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
        FileRepo instance = (FileRepo)container.getContext().lookup("java:global/classes/FileBean");
        List expResult = null;
        List result = instance.getUploadArchive();
//        assertEquals(expResult, result);
    }
}
