package Output;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class OutputMMap implements OutPut {
	
	//private String outPutString;
	private long startT;
	private long endT;
	private File file;
	private int bufferSize;
	private long countValues;
	
	private int bytesPerStep;
	private int bufferLap = 0; // toda vez que o buffer enche damos ++ aqui e recomeçamos o memory map

	
	
	private FileChannel fileChannel;
	private MappedByteBuffer mappedByteBuffer;

	@SuppressWarnings("resource")
	public OutputMMap(String fileName, String header, String outPutString, int bufferSize, boolean overwrite, String nullValue, int numWords) throws IOException 
	{
		super();	
		file = new File(fileName);
		startT = System.currentTimeMillis();
		
		setNumWords(numWords);
		
		bufferLap = 0;
		
		this.bufferSize = bufferSize;
		System.out.println("buffer size = " +bufferSize);
		fileChannel = new RandomAccessFile(file, "rw").getChannel();
		mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, this.bufferSize );
		
		countValues = 0;
		
	}
	
	public void makeOutPutString() {
		// do nothing
	}

	void setNumWords(int n) // double values (metricValue) take two words.
	{
		bytesPerStep = n*4;
	}
	
	@Override
	public synchronized void setValue(Long itemID, Long useroritemID, Double metricValue) {
		countValues++;
		
		//System.out.println(itemID + ", " + userID + " = " + metricValue.floatValue());
		
		/*
		System.out.println(mappedByteBuffer.capacity());
		System.err.println(mappedByteBuffer.position());
		*/
		
		if(mappedByteBuffer.position()+ bytesPerStep > mappedByteBuffer.capacity() )  // avoid overflow, start over.
		{
			int position = mappedByteBuffer.position();
			try 
			{
				/*
				if(bufferLap == 200) // DESCOMENTAR COMO GAMBIARRA POR FALTA DE ESPACO
				{
					flush();
					bufferLap = 0;
				}
				*/
				mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, (long)mappedByteBuffer.capacity()*bufferLap + position, this.bufferSize ); // posicao correta no arquivo
				bufferLap++;

			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			if(itemID != null)
			{
				mappedByteBuffer.putInt(itemID.intValue());
			}
			
			if(useroritemID != null)
			{
				mappedByteBuffer.putInt(useroritemID.intValue());
			}
			
			if(metricValue != null) // lembrando que um double gasta 2 words
			{
				mappedByteBuffer.putDouble(metricValue.doubleValue());
			}
			/*
			if(time != null)
			{
				charBuffer.putInt(time.intValue());
			}
			*/
			
		}
		catch(java.nio.BufferOverflowException e)
		{
			System.err.println("java.nio.BufferOverflowException Error writing "+itemID+";"+useroritemID+";"+metricValue+";"+this);
		}
		
	}

	@Override
	public synchronized void setValue(Long itemID, Double metricValue) {
		setValue(itemID, null, metricValue) ;
	}
	
	@Override
	public synchronized void setValue(Double metricValue) {
		setValue(null, null, metricValue) ;
	}
	
	@Override
	public void finish(){
		try 
		{	
			mappedByteBuffer.force();
			fileChannel.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		endT = System.currentTimeMillis();
		long tot = endT-startT;
		System.out.println(String.format("Output count %s , Time(ms) %s ",countValues, tot));
		
	}

	@Override
	public void flush() {
		mappedByteBuffer.force();
		System.err.println("Flushing mmap");
		
	}

	@Override
	public void setValueForce(Long itemID, Long userID, Double metricValue) {
		System.err.println("Error: setValueForce not found");
	}


	
	
}
