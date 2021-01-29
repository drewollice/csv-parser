import java.io.Serializable;

/**
 *  User Id (string)
 *  First and Last Name (string)
 *  Version (integer)
 *  Insurance Company (string)
 */
public final class Enrollee implements Serializable, Comparable<Enrollee>
{
	private String userId;
	private String firstName;
	private String lastName;
	private int version;
	private String insuranceCompany;
	
	@Override
	public String toString ()
	{
		return this.userId + "," + this.firstName + " " + this.lastName + "," + this.version + "," + this.insuranceCompany;
	}
	
	@Override
	public int compareTo (final Enrollee enrollee)
	{
		// Sort on surname primarily and given name secondarily.
		return 0 == this.lastName.compareTo(enrollee.getLastName())
			   ? this.firstName.compareTo(enrollee.getFirstName())
			   : this.lastName.compareTo(enrollee.getLastName());
	}
	
	public String getUserId ()
	{
		return userId;
	}
	
	public void setUserId (String userId)
	{
		this.userId = userId;
	}
	
	public String getFirstName ()
	{
		return firstName;
	}
	
	public void setFirstName (String firstName)
	{
		this.firstName = firstName;
	}
	
	public String getLastName ()
	{
		return lastName;
	}
	
	public void setLastName (String lastName)
	{
		this.lastName = lastName;
	}
	
	public int getVersion ()
	{
		return version;
	}
	
	public void setVersion (int version)
	{
		this.version = version;
	}
	
	public String getInsuranceCompany ()
	{
		return insuranceCompany;
	}
	
	public void setInsuranceCompany (String insuranceCompany)
	{
		this.insuranceCompany = insuranceCompany;
	}
}
