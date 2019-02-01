package Output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

//TODO implementar um output que seja independente dos campos, setValue pode receber um vetor de valores e o construtor pode receber o numero e o tipo de cada valor na ordem em que serao definidos
//TODO O temporizador de output deveria estar aqui e o registro ser feito ao final, para toda a metrica, no finish
public class OutPutFile implements OutPut {
	private File file;
	private FileWriter fw;
	private BufferedWriter bw;
	private int countValues;
	private int bufferSize;
	private String outPutString;
	private String header;
	private String nullValue;
	private Vector<String> stringVector;
	private int itemIDPosition;
	private int userIDPosition;
	private int valuePosition;
	private int timePosition;
	private int item2IDPosition;
	private boolean existItem;
	private boolean existUser;
	private boolean existValue;
	private boolean existTime;
	private boolean existItem2;

	public OutPutFile(String fileName, String header, String outPutString, int bufferSize, boolean overwrite, String nullValue) throws IOException {
		super();
		this.file = new File(fileName);
		file.getParentFile().mkdirs();
		this.bufferSize = bufferSize;
		this.fw = new FileWriter(file, !overwrite);
		this.bw = new BufferedWriter(fw, bufferSize);
		this.outPutString = outPutString;
		this.countValues = 0;
		this.stringVector = new Vector<String>();
		this.itemIDPosition = 0;
		this.userIDPosition = 0;
		this.valuePosition = 0;
		this.timePosition = 0;
		this.item2IDPosition = 0;
		this.existItem = false;
		this.existUser = false;
		this.existValue = false;
		this.existTime = false;
		this.existItem2 = false;
		this.header = header;
		this.nullValue = nullValue;

		if (header != null && !header.isEmpty()) {
			bw.write(this.header);
			bw.newLine();
			bw.flush();
		}
		makeOutPutString();
	}

	public void makeOutPutString() {
		StringTokenizer aux = new StringTokenizer(outPutString, "#");
		while (aux.hasMoreTokens()) {
			String currentWord = aux.nextToken();
			stringVector.add(currentWord);
			switch (currentWord) {
				case "itemID":
					itemIDPosition = stringVector.size() - 1;
					existItem = true;
					break;
				case "userID":
					userIDPosition = stringVector.size() - 1;
					existUser = true;
					break;
				case "value":
					valuePosition = stringVector.size() - 1;
					existValue = true;
					break;
				case "time":
					timePosition = stringVector.size() - 1;
					existTime = true;
					break;
				case "item2ID":
					item2IDPosition = stringVector.size() - 1;
					existItem2 = true;
					break;
			}
		}
	}

	public synchronized void setValue(Double metricValue)
	{
		try {
			bw.write(metricValue.toString());
			bw.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public synchronized void setValue(Long itemID, Long useroritemID, Double metricValue) {
		if (existItem)
			this.stringVector.set(itemIDPosition, itemID == null ? nullValue : Long.toString(itemID));
		if (!existItem && existUser && useroritemID == null) // gb: cb dava erro
			this.stringVector.set(userIDPosition, itemID == null ? nullValue : Long.toString(itemID));
		else if (existUser)
			this.stringVector.set(userIDPosition, useroritemID == null ? nullValue : Long.toString(useroritemID));
		if (existValue)
			this.stringVector.set(valuePosition, metricValue == null ? nullValue : Double.toString(metricValue));
		if (existItem2)
			this.stringVector.set(item2IDPosition, useroritemID == null ? nullValue : Long.toString(useroritemID));

		try {
			for (int i = 0; i < stringVector.size(); i++) {
				bw.write(stringVector.get(i));
			}
			bw.newLine();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return;
	}
	
	public synchronized void setValue(Long itemID, Double metricValue) 
		{
		setValue(itemID, null, metricValue);
		}
	
	public synchronized void setValueForce(Long itemID, Long userID, Double metricValue) {
			//System.out.println("write: "+itemID+" "+userID+" "+metricValue);
			this.stringVector.set(0, Long.toString(itemID)+";");
			this.stringVector.set(1, Long.toString(userID)+";");
			this.stringVector.set(2, Double.toString(metricValue)+";");

		try {
			for (int i = 0; i < stringVector.size(); i++) {
				bw.write(stringVector.get(i));
			}
			bw.newLine();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public synchronized void flush() {
		try {
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void finish() {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getCountValues() {
		return countValues;
	}

	public void setCountValues(int countValues) {
		this.countValues = countValues;
	}

	protected int getBufferSize() {
		return bufferSize;
	}

	protected void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}
