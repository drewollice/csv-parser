import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 6.	Coding exercise:  Availity receives enrollment files from various benefits management and enrollment solutions
 * (I.e., HR platforms, payroll platforms).  Most of these files are typically in EDI format.
 * However, there are some files in CSV format. For the files in CSV format,
 * <p>
 * Write a program that will read the content of the file
 * and separate enrollees by insurance company in its own file.
 * <p>
 * Additionally, sort the contents of each file by last and first name (ascending).
 * <p>
 * Lastly, if there are duplicate User Ids for the same Insurance Company,
 * then only the record with the highest version should be included.
 * <p>
 * The following data points are included in the file:
 * •	User Id (string)
 * •	First and Last Name (string)
 * •	Version (integer)
 * •	Insurance Company (string)
 */
public class EnrollmentProcessing
{
	public static void main (final String[] args)
	{
		// Chop path, name, and extension.
		final String BACKSLASH_WITH_DELIMITER = "((?<=\\\\\\\\)|(?=\\\\\\\\))";
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args[0].split(BACKSLASH_WITH_DELIMITER).length-1; i++)
		{
			sb.append(args[0].split(BACKSLASH_WITH_DELIMITER)[i]);
		}
		final String filePath = sb.toString();
		final String fileName = args[0].split("\\\\")[args[0].split("\\\\").length-1].split("\\.")[0];
		final String fileExt = "." + args[0].split("\\.")[1];
		
		// Pull data.
		final Collection<Enrollee> ingestedData = ingestData(args[0]);
		
		// Process data.
		Map<String, Collection<Enrollee>> dataPerInsuranceCo = separateByInsuranceCo(ingestedData);
		dataPerInsuranceCo = stripDupesByVersion(dataPerInsuranceCo);
		dataPerInsuranceCo = sortByNames(dataPerInsuranceCo);
		
		// Output data.
		dataPerInsuranceCo.forEach((insuranceCo, enrollees) -> writeToFile(filePath + fileName + "_" + insuranceCo + fileExt, enrollees));
	}
	
	/**
	 * Collect the data from a CSV formatted file and turn each record into an Enrollee object.
	 *
	 * @param file The file path + file name + file extension of the file to be ingested.
	 *
	 * @return The Collection of Enrollees derived from each row in the CSV file.
	 */
	private static Collection<Enrollee> ingestData (final String file)
	{
		final Collection<Enrollee> enrollees = new ArrayList<>();
		
		// Create a reader object to be used reading the file.
		try (BufferedReader rows = new BufferedReader(new FileReader(file)))
		{
			// Create variables to be re-used each pass thru the loop.
			String row;
			String[] columns;
			Enrollee enrollee;
			
			// Loop thru each row of the file; create an object for each.
			while ((row = rows.readLine()) != null)
			{
				columns = row.split(",");
				
				enrollee = new Enrollee();
				
				enrollee.setUserId(columns[0]);
				enrollee.setFirstName(columns[1].split(" ")[0]);
				enrollee.setLastName(columns[1].split(" ")[1]);
				enrollee.setVersion(Integer.parseInt(columns[2]));
				enrollee.setInsuranceCompany(columns[3]);
				
				enrollees.add(enrollee);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return enrollees;
	}
	
	/**
	 * Additionally, sort the contents of each file by last and first name (ascending).
	 *
	 * @param enrollees The Colleciton of the Enrollees to be separated.
	 *
	 * @return A Map using the Insurance Company as the Key and a Collection of associated Enrollee fields as the Value.
	 */
	private static Map<String, Collection<Enrollee>> separateByInsuranceCo (final Collection<Enrollee> enrollees)
	{
		final Map<String, Collection<Enrollee>> dataPerInsuranceCo = new HashMap<>();
		
		// Loop thru enrollees
		for (final Enrollee enrollee : enrollees)
		{
			// If this enrollee's insurance co is unique, push a new entry to the map with all enrollees of like insurance co.
			dataPerInsuranceCo.putIfAbsent(enrollee.getInsuranceCompany(), enrollees.stream().filter(enrollee1 -> enrollee.getInsuranceCompany().equals(enrollee1.getInsuranceCompany())).collect(Collectors.toList()));
		}
		
		return dataPerInsuranceCo;
	}
	
	/**
	 * Lastly, if there are duplicate User Ids for the same Insurance Company,
	 * then only the record with the highest version should be included.
	 *
	 * @param dataPerInsuranceCo The Map containing data to be stripped of duplicates.
	 *
	 * @return The same map but updated without duplicates.
	 */
	private static Map<String, Collection<Enrollee>> stripDupesByVersion (final Map<String, Collection<Enrollee>> dataPerInsuranceCo)
	{
		final Map<String, Collection<Enrollee>> strippedData = new HashMap<>();
		
		// Loop thru each insurance company entry.
		dataPerInsuranceCo.forEach((insuranceCo, enrollees) ->
								   {
									   // Map for the temporary purpose of rooting out duplicates.
									   final Map<String, Enrollee> dupeFree = new HashMap<>();
			
									   // Loop thru enrollees of this insurance company entry.
									   for (final Enrollee enrollee : enrollees)
									   {
										   // Check for duplicate enrollee userId fields.
										   if (dupeFree.containsKey(enrollee.getUserId()))
										   {
											   // In event of duplicates, add the entry with the highest version.
											   if (enrollee.getVersion() > dupeFree.get(enrollee.getUserId()).getVersion())
											   {
												   dupeFree.put(enrollee.getUserId(), enrollee);
											   }
										   }
										   // If not duplicate, just add the entry.
										   else
										   {
											   dupeFree.put(enrollee.getUserId(), enrollee);
										   }
									   }
			
									   // After all enrollees of this insurance company are handled, push them to the map.
									   enrollees.clear();
									   enrollees.addAll(dupeFree.values());
									   strippedData.put(insuranceCo, enrollees);
								   });
		
		return strippedData;
	}
	
	/**
	 * Additionally, sort the contents of each file by last and first name (ascending).
	 *
	 * @param dataPerInsuranceCo The Map containing data to be sorted.
	 *
	 * @return The same map but sorted.
	 */
	private static Map<String, Collection<Enrollee>> sortByNames (final Map<String, Collection<Enrollee>> dataPerInsuranceCo)
	{
		final Map<String, Collection<Enrollee>> sortedData = new HashMap<>();
		
		// Sort each based on the custom compareTo function in the Enrollee class.
		dataPerInsuranceCo.forEach((insuranceCo, enrollees) -> sortedData.put(insuranceCo, enrollees.stream().sorted(Enrollee :: compareTo).collect(Collectors.toList())));
		
		return sortedData;
	}
	
	/**
	 * This method serves to write one enrollee record per line into a CSV file.
	 *
	 * @param fileName  The file path + file name + file extension of the file to be ingested.
	 * @param enrollees The Collection of Enrollee records to be written the the file.
	 */
	private static void writeToFile (final String fileName, final Collection<Enrollee> enrollees)
	{
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(updateFileName(fileName))))
		{
			// For each enrollee, write them to the file then new line.
			for (final Enrollee enrollee : enrollees)
			{
				writer.write(enrollee.toString());
				writer.newLine();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This method appends a number to the end of the filename if the filename is already taken.
	 *
	 * @param fileName The file name to possibly be updated.
	 *
	 * @return The updated file name if needed. Otherwise the name will be returned unchanged.
	 */
	private static String updateFileName (final String fileName)
	{
		final File file = new File(fileName);
		
		// If the file exists, rename it.
		if (file.exists())
		{
			// Chop name and extension.
			final String name = fileName.split("\\.")[0];
			final String extension = fileName.split("\\.")[1];
			
			String updatedFileName = name;
			
			// Increment by 1 until a unique name is found.
			for (int i = 1; new File(updatedFileName + "." + extension).exists(); i++)
			{
				updatedFileName = name + "-" + i;
			}
			
			// Return the new file name with the extension attached.
			return updatedFileName + "." + extension;
		}
		
		return fileName;
	}
}
