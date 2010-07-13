///////////////////////////////////////////////////////////////////////////////
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////
package gnu.trove.generate;

import java.io.*;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Generates classes that are created from templates. The "*.template" files must be in
 * the classpath (because Class.getResource() is used to load the files).
 */
public class Generate {
    private static final String P2P_MAP_DECORATOR_NAME = "P2PMapDecorator.template";
    private static final WrapperInfo[] WRAPPERS = new WrapperInfo[] {
        new WrapperInfo( "double", "Double", "POSITIVE_INFINITY", "NEGATIVE_INFINITY" ),
        new WrapperInfo( "float", "Float", "POSITIVE_INFINITY", "NEGATIVE_INFINITY" ),
        new WrapperInfo( "int", "Integer", "MAX_VALUE", "MIN_VALUE" ),
        new WrapperInfo( "long", "Long", "MAX_VALUE", "MIN_VALUE" ),
        new WrapperInfo( "byte", "Byte", "MAX_VALUE", "MIN_VALUE" ),
        new WrapperInfo( "short", "Short", "MAX_VALUE", "MIN_VALUE" ) };


    public static void main(String[] args) throws IOException {
        if ( args.length == 0 ) {
            System.out.println( "Usage: Generate <output_path>" );
            return;
        }

        File output_path = new File( args[ 0 ] );
        if ( !output_path.exists() ) {
            System.err.println( "\"" + output_path + "\" does not exist" );
            return;
        }


        generateP2PMapDecorators(output_path);

        // Decorators
        generate("P2OMapDecorator.template", "decorator/T", "ObjectHashMapDecorator.java", output_path);
        generate("O2PMapDecorator.template", "decorator/TObject", "HashMapDecorator.java", output_path);
        generate("PHashSetDecorator.template", "decorator/T", "HashSetDecorator.java", output_path);

        // Iterators
        generate("O2PIterator.template", "TObject", "Iterator.java", output_path);
        generate("P2OIterator.template", "T", "ObjectIterator.java", output_path);
        generateP2P("P2PIterator.template", "T", "Iterator.java", output_path);
        generate("PIterator.template", "T", "Iterator.java", output_path);

        // Procedures
        generate("O2PProcedure.template", "TObject", "Procedure.java", output_path);
        generate("P2OProcedure.template", "T", "ObjectProcedure.java", output_path);
        generateP2P("P2PProcedure.template", "T", "Procedure.java", output_path);
        generate("PProcedure.template", "T", "Procedure.java", output_path);

        // Functions
        generate("PFunction.template", "T", "Function.java", output_path);

        // Hashing Strategy interfaces
        generate("PHashingStrategy.template", "T", "HashingStrategy.java", output_path);

        // Hash abstract classes
        generate("PHash.template", "T", "Hash.java", output_path);

        // HashMaps
        generate("P2OHashMap.template", "T", "ObjectHashMap.java", output_path);
        generate("O2PHashMap.template", "TObject", "HashMap.java", output_path);
        generateP2P("P2PHashMap.template", "T", "HashMap.java", output_path);

        // ArrayLists
        generate( "PArrayList.template", "T", "ArrayList.java", output_path);

        // HashSets
        generate( "PHashSet.template", "T", "HashSet.java", output_path);

        // Stacks
        generate( "PStack.template", "T", "Stack.java", output_path);

        System.out.println( "Generation complete." );
    }

    private static void generate(String templateName, String pathPrefix, String pathSuffix,
        File output_path) throws IOException {

        String template = readFile(templateName);
        for (int i = 0; i < WRAPPERS.length; i++) {
            WrapperInfo info = WRAPPERS[ i ];

            String e = info.primitive;
            String ET = info.class_name;
            String E = shortInt(ET);
            String EMAX = info.max_value;
            String EMIN = info.min_value;
            String out = template;

            out = Pattern.compile("#e#").matcher(out).replaceAll(e);
            out = Pattern.compile("#E#").matcher(out).replaceAll(E);
            out = Pattern.compile("#ET#").matcher(out).replaceAll(ET);
            out = Pattern.compile("#EMAX#").matcher(out).replaceAll(EMAX);
            out = Pattern.compile("#EMIN#").matcher(out).replaceAll(EMIN);

            String outFile = pathPrefix + E + pathSuffix;
            writeFile(outFile, out, output_path);
        }
    }

    private static void generateP2P(String templateName, String pathPrefix,
        String pathSuffix, File output_path) throws IOException {

        String template = readFile(templateName);
        for (int i = 0; i < WRAPPERS.length; i++) {
            WrapperInfo info = WRAPPERS[ i ];

            String e = info.primitive;
            String ET = info.class_name;
            String E = shortInt(ET);
            String EMAX = info.max_value;
            String EMIN = info.min_value;

            for( int j = 0; j < WRAPPERS.length; j++ ) {
                String out = template;
                out = Pattern.compile("#e#").matcher(out).replaceAll(e);
                out = Pattern.compile("#E#").matcher(out).replaceAll(E);
                out = Pattern.compile("#ET#").matcher(out).replaceAll(ET);
                out = Pattern.compile("#EMAX#").matcher(out).replaceAll(EMAX);
                out = Pattern.compile("#EMIN#").matcher(out).replaceAll(EMIN);

                WrapperInfo jinfo = WRAPPERS[ j ];

                String f = jinfo.primitive;
                String FT = jinfo.class_name;
                String F = shortInt(FT);
                String FMAX = jinfo.max_value;
                String FMIN = jinfo.min_value;

                out = Pattern.compile("#f#").matcher(out).replaceAll(f);
                out = Pattern.compile("#F#").matcher(out).replaceAll(F);
                out = Pattern.compile("#FT#").matcher(out).replaceAll(FT);
                out = Pattern.compile("#FMAX#").matcher(out).replaceAll(FMAX);
                out = Pattern.compile("#FMIN#").matcher(out).replaceAll(FMIN);

                String outFile = pathPrefix + E + F + pathSuffix;
                writeFile(outFile, out, output_path);
            }
        }
    }

    private static void generateP2PMapDecorators(File output_path) throws IOException {
        String template = readFile(P2P_MAP_DECORATOR_NAME);
        for (int i = 0; i < WRAPPERS.length; i ++) {
            WrapperInfo info = WRAPPERS[ i ];

            String k = info.primitive;
            String KT = info.class_name;
            String K = shortInt(KT);
            String KMAX = info.max_value;
            String KMIN = info.min_value;
            
            for (int j = 0; j < WRAPPERS.length; j++) {
                WrapperInfo jinfo = WRAPPERS[ j ];

                String v = jinfo.primitive;
                String VT = jinfo.class_name;
                String V = shortInt(VT);
                String VMAX = jinfo.max_value;
                String VMIN = jinfo.min_value;

                String out = template;

                out = Pattern.compile("#v#").matcher(out).replaceAll(v);
                out = Pattern.compile("#V#").matcher(out).replaceAll(V);
                out = Pattern.compile("#k#").matcher(out).replaceAll(k);
                out = Pattern.compile("#K#").matcher(out).replaceAll(K);
                out = Pattern.compile("#KT#").matcher(out).replaceAll(KT);
                out = Pattern.compile("#VT#").matcher(out).replaceAll(VT);
                out = Pattern.compile("#KMAX#").matcher(out).replaceAll(KMAX);
                out = Pattern.compile("#VMAX#").matcher(out).replaceAll(VMAX);
                out = Pattern.compile("#KMIN#").matcher(out).replaceAll(KMIN);
                out = Pattern.compile("#VMIN#").matcher(out).replaceAll(VMIN);
                String outFile = "decorator/T" + K + V + "HashMapDecorator.java";
                writeFile(outFile, out, output_path);
            }
        }
    }

    private static void writeFile(String file, String out, File output_path) throws IOException {
        File destination = new File( output_path.getPath() + "/" + file );
        File parent = destination.getParentFile();
        parent.mkdirs();

		// Write to a temporary file
		File temp = File.createTempFile( "trove", "gentemp", new File( "output" ) );
		FileWriter writer = new FileWriter( temp );
        writer.write(out);
        writer.close();


        // Now determine if it should be moved to the final location
		final boolean need_to_move;
		if ( destination.exists() ) {
			boolean matches;
			try {
				MessageDigest digest = MessageDigest.getInstance( "MD5" );

                byte[] current_file = digest( destination, digest );
                byte[] new_file = digest( temp, digest );

                matches = Arrays.equals( current_file, new_file );
            }
			catch( NoSuchAlgorithmException ex ) {
				System.err.println( "WARNING: Couldn't load digest algorithm to compare " +
					"new and old template. Generation will be forced." );
				matches = false;
			}

			need_to_move = !matches;
		}
        else need_to_move = true;


        // Now move it if we need to move it
        if ( need_to_move ) {
            destination.delete();
            if ( !temp.renameTo( destination ) ) {
                throw new IOException( "ERROR writing: " + destination );
            }
            else System.out.println( "Wrote: " + destination );
        }
        else {
            System.out.println( "Skipped: " + destination );
            temp.delete();
        }
    }


	private static byte[] digest( File file, MessageDigest digest ) throws IOException {
		digest.reset();

		byte[] buffer = new byte[ 1024 ];
		FileInputStream in = new FileInputStream( file );
		try {
			int read = in.read( buffer );
			while( read >= 0 ) {
				digest.update( buffer, 0, read );

				read = in.read( buffer );
			}

			return digest.digest();
		}
		finally {
			try {
				in.close();
			}
			catch( IOException ex ) {
				// ignore
			}
		}
	}



	private static String shortInt(String type) {
        return type.equals("Integer") ? "Int" : type;
    }

    private static String readFile(String name) throws IOException {
        String packageName = Generate.class.getPackage().getName();
        URL resource = Generate.class.getClassLoader().getResource(packageName.replace('.','/')+"/"+name);
        if (resource == null) {
            throw new NullPointerException( "Couldn't find: " +
                packageName.replace('.','/')+"/"+name );
        }
        InputStream inputStream = resource.openConnection().getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder out = new StringBuilder();

        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            out.append(line);
            out.append("\n");
        }
        return out.toString();
    }


    private static class WrapperInfo {
        final String primitive;
        final String class_name;
        final String max_value;
        final String min_value;

        WrapperInfo( String primitive, String class_name, String max_value,
            String min_value ) {

            this.primitive = primitive;
            this.class_name = class_name;
            this.max_value = class_name + "." + max_value;
            this.min_value = class_name + "." + min_value;
        }
    }
}
