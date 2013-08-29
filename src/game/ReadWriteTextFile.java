package game;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.*;
import org.jdom.input.*;

public class ReadWriteTextFile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1285769800797087619L;
	
	private static final Logger logger = Logger.getLogger(ReadWriteTextFile.class);
	
	
	public String getContent(File aFile,String[] elName){
		SAXBuilder parser = new SAXBuilder();
		try {
			Document doc = parser.build(aFile);
			Element root = doc.getRootElement();
			Element el = root;
			//iterate on every part of the given path
			for(int j=0;j<elName.length;j++){
				// elName is of format Name,attribute:value
				String s = elName[j];
				String[] t = s.split(",");
					try{
						String[] u = t[1].split(":");
						@SuppressWarnings("unchecked")
						List<Element> children = el.getChildren(t[0]);
						//iterate over children to find the one with the right attribute
						for(Element e: children){
							if(e.getAttributeValue(u[0]).equalsIgnoreCase(u[1])){
								el = e;
								break;
							}
						}
					}catch(ArrayIndexOutOfBoundsException e){
						//no attribute specified, child is simply first child with name s
						el = el.getChild(s);
					}catch(Exception e){
						e.printStackTrace();
						break;
					}
			}
			return el.getTextTrim();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(NullPointerException e){
			e.printStackTrace();
		}
		return null;
	}

	public Object parseData(String s){
		// try to split strings into multiple strings in case of multiple IDs
		// in case of a single int, ints will contain the one int
		String[] ints = s.split(";");
		try{
			ArrayList<Integer> IDs = new ArrayList<Integer>();
			for(String str: ints){
				IDs.add(Integer.parseInt(str));
			}
			if(IDs.size() == 1 && !s.contains(";")) return (int)IDs.get(0);
			return convertIntegers(IDs);
		} catch(NumberFormatException e){// thrown when s is a string or double, float..
			ints = null;
			try{
				return Double.parseDouble(s);
			} catch(NumberFormatException nfe){
				if(s.equalsIgnoreCase("true")){
					return true;
				}
				else if(s.equalsIgnoreCase("false")){
					return false;
				}
				return s;
			}
		}
	}

	public Object loadData(Class<?> cl, Element el){
		// creates a class of given type, coming from the XML element el
		
		ArrayList<Object> constrParam = new ArrayList<Object>();
		@SuppressWarnings("unchecked")
		List<Element> properties = el.getChildren();
		// id is given as attribute for xquery, add id to constrparam first
		constrParam.add(Integer.parseInt(el.getAttributeValue("id")));
		Iterator<Element> i = properties.iterator();
		while(i.hasNext()){
			Element child = i.next();
			// if child has more children, those are dealt with in the constructor, child is passed as a parameter
			if(!child.getChildren().isEmpty()){
				constrParam.add(child);
			}
			else{
				constrParam.add(parseData(child.getText()));
			}
		}
		Class<?>[] paramClass = new Class[constrParam.size()];//soorten parameters
		for(int j = 0;j<paramClass.length;j++){
			paramClass[j] = constrParam.get(j).getClass();
		}
		try {
			Constructor<?> objCtor = cl.getConstructor(paramClass);
			return objCtor.newInstance(constrParam.toArray());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			logger.debug("No constructor found for class " + cl.getName() + " for element " + el.getChildText("name"), e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			logger.debug("Error for class " + cl.getName() + " for element " + el.getChildText("name"), e);
		} catch (InstantiationException e) {
			e.printStackTrace();
			logger.debug("Error for class " + cl.getName() + " for element " + el.getChildText("name"), e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			logger.debug("Error for class " + cl.getName() + " for element " + el.getChildText("name"), e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			logger.debug("Error for class " + cl.getName() + " for element " + el.getChildText("name"), e);
		}
		return null;
	}

	public static int[] convertIntegers(ArrayList<Integer> integers)//ArrayList -> int[]
	{
		int[] ret = new int[integers.size()];
		for (int i=0; i < ret.length; i++)
		{
			ret[i] = integers.get(i).intValue();
		}
		return ret;
	}

	/**
	 * Change the contents of text file in its entirety, overwriting any
	 * existing text.
	 *
	 * This style of implementation throws all exceptions to the caller.
	 *
	 * @param aFile is an existing file which can be written to.
	 * @throws IllegalArgumentException if param does not comply.
	 * @throws FileNotFoundException if the file does not exist.
	 * @throws IOException if problem encountered during write.
	 */
	static public void setContents(File aFile, String aContents,boolean append)
	throws FileNotFoundException, IOException {
		if (aFile == null) {
			throw new IllegalArgumentException("File should not be null.");
		}
		if (!aFile.exists()) {
			throw new FileNotFoundException ("File does not exist: " + aFile);
		}
		if (!aFile.isFile()) {
			throw new IllegalArgumentException("Should not be a directory: " + aFile);
		}
		if (!aFile.canWrite()) {
			throw new IllegalArgumentException("File cannot be written: " + aFile);
		}

		//use buffering
		Writer output = new BufferedWriter(new FileWriter(aFile,append));
		try {
			//FileWriter always assumes default encoding is OK!
			output.write( aContents );
		}
		finally {
			output.close();
		}
	}

}