/**
 * class to generate random number for exponential distribution
 * of csExecution time & interRequest time.
 *
 */
public class ExpProbTime{
	protected double var = 1.0;
	public ExpProbTime(double var){
		this.var = var;
	}
	
	public int RandomNum(){
		return (int)(-var * Math.log(Math.random()));
	}
	
}