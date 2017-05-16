package main;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class GoldwasserMicali_Test1 {
	public final static int SecurityParameter=200;
	public final static DateFormat format=new SimpleDateFormat("MM/dd HH:mm:ss");
	public static class PublicKeyPair{
		BigInteger Modulus;
		BigInteger Q_Nonresidue;
		public void Print(){
			System.out.println("PublicKeyPair:\nModulus:"+Modulus.toString(16)+"\nQuadratic_Nonresidue:"+Q_Nonresidue.toString(16)+"\nTime:"+format.format(new Date())+"\n");
		}
	}
	public static class PrivateKeyPair{//PrKP.Pr1,PrKP.Pr2:Two PrivateKeys,factors of Modulus
		BigInteger Pr1;
		BigInteger Pr2;
		public void Print(){
			System.out.println("PrivateKeyPair:\nPrivate factor1:"+Pr1.toString(16)+"\nPrivate factor2:"+Pr2.toString(16)+"\nTime:"+format.format(new Date())+"\n");
		}
	}
	
	public static void main(String[] args) throws Exception {
		PrivateKeyPair TestPr=PrKGenerator();
		TestPr.Print();
		PublicKeyPair TestPu=PuKGenerator(TestPr);		
		TestPu.Print();
		byte TestByte=(byte)57;
		System.out.println("Test byte:"+TestByte+"\nSecurityParameter:"+SecurityParameter+"\nEncrypted:");
		BigInteger[] Ciphertext=encrypt(TestByte,TestPu);
		for(int i=0;i<Ciphertext.length;i++)System.out.println("c"+i+":"+Ciphertext[i].toString(16));
		System.out.println("Time:"+format.format(new Date()));
		byte x;
		x=decrypt(Ciphertext,TestPr);
		System.out.println("Decrypted:"+x);
		System.out.println("Time:"+format.format(new Date()));
	}
	
	public static boolean[] ByteToBoolean(byte input){
		boolean[] output=new boolean[8];
		for(int i=output.length-1;i>=0;i--){
			if(input%2==0)output[i]=false;
			else output[i]=true;
			input=(byte) (input>>>1);
		}
		return output;
	}
	
	public static byte BooleanToByte(boolean[] input){
		byte output=0;
		for(int i=0;i<input.length;i++){
			if(input[i])output++;
			if(i!=input.length-1)output=(byte) (output<<1);
		}
		return output;	
	}
	
	public static BigInteger PrimeGenerator(){
		BigInteger prime;
		Random rnd=new Random(new Date().getTime());
		prime=BigInteger.probablePrime(SecurityParameter, rnd);//bitLength but not Decimal
		BigInteger exponent=prime.subtract(new BigInteger("1"));
		BigInteger a=new BigInteger("1147");
		BigInteger r=a.modPow(exponent,prime);//Fermat little theorem
		if(r.compareTo(new BigInteger("1"))==0)System.out.println("a:"+a+";exponent:"+exponent.toString(16)+";prime:"+prime.toString(16)+";[a^(p-1)]mod p=r:"+r);
		return prime;
	}
	
	public static PrivateKeyPair PrKGenerator(){
		PrivateKeyPair PrKP=new PrivateKeyPair();
		PrKP.Pr1=PrimeGenerator();
		PrKP.Pr2=PrimeGenerator();
		return PrKP;
	}
	
	public static boolean LegendreSymbolCalculate(BigInteger integer,BigInteger Modulus)throws Exception{
		int i=1,r=1;
		BigInteger Num0=new BigInteger("0");
		BigInteger Num1=new BigInteger("1");
		if(integer.gcd(Modulus).compareTo(Num1)!=0)throw new Exception("The integer and Modulus are not coprime");
		
		BigInteger Num2=new BigInteger("2");
		BigInteger Num8=new BigInteger("8");
		BigInteger temp;
		while(true){
			while(integer.mod(Num2).compareTo(Num0)==0){
				integer=integer.divide(Num2);
				i=i*(-1);
			}
			if(((((Modulus.pow(2)).subtract(Num1)).divide(Num8)).mod(Num2)).compareTo(Num0)!=0)r=r*i;
			//if the condition is false,then [(p^2)-1]/8 is an even number,i^[(p^2)-1]/8=1,no matter i is 1 or -1
			i=1;
			if(integer.compareTo(Num1)==0)break;
			if(integer.compareTo(Modulus)<0){
				temp=integer.subtract(Num1).divide(Num2);
				temp=temp.multiply((Modulus.subtract(Num1).divide(Num2)));
				if(temp.mod(Num2).compareTo(Num0)!=0)r=r*(-1);
				temp=integer;
				integer=Modulus;
				Modulus=temp;
			}
			/*System.out.println("q:"+integer);
			System.out.println("p:"+Modulus);
			System.out.println("r:"+r);*/
			integer=integer.mod(Modulus);
		}
		if(r==1)return true;
		else return false;
	}
	
	public static BigInteger CoprimeGenerator(BigInteger Modulus,int Length){
		BigInteger CoprimeNum;
		//BigInteger a,b,residue;
		Random rnd;
		do{
			rnd=new Random(new Date().getTime());
			CoprimeNum=BigInteger.probablePrime(Length, rnd);
			/*a=Modulus;
			b=CoprimeNum;
			residue=new BigInteger("0");
			do{
				residue=a.mod(b);
				a=b;
				b=residue;
			}while(residue.compareTo(new BigInteger("0"))==0);
		}while(a.compareTo(new BigInteger("1"))!=0);*/
		}while(CoprimeNum.gcd(Modulus).compareTo(new BigInteger("1"))!=0);//already had a gcd algorithm
		return CoprimeNum;
	}
	
	public static PublicKeyPair PuKGenerator(PrivateKeyPair PrKP) throws Exception{
		PublicKeyPair PuKP=new PublicKeyPair();
		boolean a,b;
		BigInteger Coprime;
		PuKP.Modulus=PrKP.Pr1.multiply(PrKP.Pr2);
		do{
			Coprime=CoprimeGenerator(PuKP.Modulus,PuKP.Modulus.bitLength());
			a=LegendreSymbolCalculate(Coprime,PrKP.Pr1);
			b=LegendreSymbolCalculate(Coprime,PrKP.Pr2);
		}while(a||b);//a||b then y may or may not be a pseudo quadratic residue,a&&b then y must be a pseudo quadratic residues
		PuKP.Q_Nonresidue=Coprime;
		return PuKP;
	}
	
	public static BigInteger[] encrypt(byte Plaintext,PublicKeyPair PuKP){
		BigInteger[] Ciphertext=new BigInteger[8];
		BigInteger integer;
		boolean[] RawData=ByteToBoolean(Plaintext);
		for(int i=0;i<RawData.length;i++){
			integer=CoprimeGenerator(PuKP.Modulus,PuKP.Modulus.bitLength());
			if(RawData[i])Ciphertext[i]=integer.pow(2).mod(PuKP.Modulus);
			else Ciphertext[i]=integer.pow(2).multiply(PuKP.Q_Nonresidue).mod(PuKP.Modulus);
		}
		return Ciphertext;
	}
	
	public static byte decrypt(BigInteger[] Ciphertext,PrivateKeyPair PrKP) throws Exception{
		byte Plaintext;
		boolean a=false,b=false;
		boolean[] RawData=new boolean[8]; 
		for(int i=0;i<Ciphertext.length;i++){
			a=LegendreSymbolCalculate(Ciphertext[i],PrKP.Pr1);
			b=LegendreSymbolCalculate(Ciphertext[i],PrKP.Pr2);
			if(a&&b)RawData[i]=true;
			else RawData[i]=false;
		}
		Plaintext=BooleanToByte(RawData);
		return Plaintext;
	}
}
