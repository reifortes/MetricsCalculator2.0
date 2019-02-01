package Output;

import java.io.IOException;

public class OutPutHandler {
		//variaveis de formato do arquivo de saida:
		protected String ioHeaderString = "metrica;flush;tipo;tempoIO";
		protected String itemHeaderString = "item;valor"; //"item;valor;tempo";
		protected String userHeaderString = "user;valor"; //"user;valor;tempo";
		protected String itemUserHeaderString = "item;user;valor"; //"item;user;valor;tempo";
		protected String itemItemHeaderString = "item;item;valor"; //"item;item;valor;tempo";

		protected String ioOutputString = "#itemID#;#userID#;#value#";//"#itemID#;#userID#;#value#;#time#";
		protected String itemOutputString = "#itemID#;#value#"; //"#itemID#;#value#;#time#;";
		protected String userOutputString = "#userID#;#value#"; //"#userID#;#value#;#time#;";
		protected String itemUserOutputString = "#itemID#;#userID#;#value#"; //"#itemID#;#userID#;#value#;#time#;";
		protected String itemItemOutputString = "#itemID#;#item2ID#;#value#"; //"#itemID#;#item2ID#;#value#;#time#;";

		protected String nullValue = "";
		protected boolean overwrite = true;
		protected int bufferSize =    1024 * 1024 * 1024;
		
		protected OutPutFile outPutIO; // TODO Implementar um metodo melhor para registrar IO
		protected OutPut outFileItem;
		protected OutPut outFileUser;
		protected OutPut outFileItemUser;
		protected OutPut outFileItemItem;
		/* --- */
		
		/*parameters*/
		private boolean doItem = false;
		private boolean doUser = false;
		private boolean doItemUser = false;
		private boolean doItemItem = false;
		
		private boolean useMemoryMapping = true;
		private int numOfRegisters = 0;
		/* -- */
		
		
		public OutPut getOutFileItem() {
			return outFileItem;
		}

		public void setOutFileItem(OutPut outFileItem) {
			this.outFileItem = outFileItem;
		}

		public OutPut getOutFileUser() {
			return outFileUser;
		}
		
		public OutPut getOutFileItemItem() {
			return outFileItemItem;
		}
		
		public void setOutFileItemItem(OutPut outFileItemItem) {
			this.outFileItemItem = outFileItemItem;
		}

		public void setOutFileUser(OutPut outFileUser) {
			this.outFileUser = outFileUser;
		}

		public OutPut getOutFileItemUser() {
			return outFileItemUser;
		}

		public void setOutFileItemUser(OutPut outFileItemUser) {
			this.outFileItemUser = outFileItemUser;
		}

		
		
		public OutPutHandler()
		{
			
		}
		
		public OutPutHandler(String basePath, String metricName, int bufferSize, boolean overwrite) throws IOException
		{
			this.overwrite = overwrite;
			this.bufferSize = bufferSize;
			createOutput(basePath, metricName);
		}
		
		public void createOutput(String basePath, String metricName) throws IOException
		{
			createOutput(basePath, metricName, false);
		}
		
		public void createOutput(String basePath, String metricName, boolean outputTxt) throws IOException
		{
			//System.out.println("Buffer size : "+bufferSize);
			// TODO: Make it for all
						//outPutIO = new OutPutFile(basePath+metricName+"_IO.txt", ioHeaderString, ioOutputString, bufferSize, overwrite, nullValue);
						if (outputTxt)
						{
							if(doItem) outFileItem = new OutPutFile(basePath+metricName+"-01-Itemt.txt", itemHeaderString, itemOutputString, bufferSize, overwrite, nullValue);
							if(doUser) outFileUser= new OutPutFile(basePath+metricName+"-02-Usert.txt", userHeaderString, userOutputString, bufferSize, overwrite, nullValue);
							if(doItemUser) outFileItemUser = new OutPutFile(basePath+metricName+"-03-ItemUsert.txt", itemUserHeaderString, itemUserOutputString, bufferSize, overwrite, nullValue);
							if(doItemItem) outFileItemItem = new OutPutFile(basePath+metricName+"-03-ItemItemt.txt", itemItemHeaderString, itemItemOutputString, bufferSize, overwrite, nullValue);
						}
						
						else
						{
							if(doItem) outFileItem = new OutputMMap(basePath+metricName+"-01-Item.txt", itemHeaderString, itemOutputString, bufferSize, overwrite, nullValue, 3);
							if(doUser) outFileUser= new OutputMMap(basePath+metricName+"-02-User.txt", userHeaderString, userOutputString, bufferSize, overwrite, nullValue, 3);
							if(doItemUser) outFileItemUser = new OutputMMap(basePath+metricName+"-03-ItemUser.txt", itemUserHeaderString, itemUserOutputString, bufferSize, overwrite, nullValue, 4);
							
							if(doItemItem) outFileItemItem = new OutputMMap(basePath+metricName+"-03-ItemItem.txt", itemItemHeaderString, itemItemOutputString, bufferSize, overwrite, nullValue, 4);
						}
		}	
		public void finishOutput() throws IOException
		{
			if(outPutIO != null) outPutIO.finish();
			if(doItem) outFileItem.finish();
			if(doUser) outFileUser.finish(); 
			if(doItemUser) outFileItemUser.finish(); 
			if(doItemItem) outFileItemItem.finish(); 
		}
		
		public OutPutFile getOutPutIO()
		{
			return outPutIO;
		}

		public void setParameters(boolean doI, boolean doU, boolean doIU) {
			doItem = doI;
			doUser = doU;
			doItemUser = doIU;
			doItemItem = false;
		}

		public void setParameters(boolean doI, boolean doU, boolean doIU, boolean doII) {
			doItem = doI;
			doUser = doU;
			doItemUser = doIU;
			doItemItem = doII;
		}

		public void setNumOfRegisters(int numOfRegisters) 
		{
			this.numOfRegisters = numOfRegisters;	
		}
		
		public void setBufferSize(int size)
		{
			bufferSize = size;
		}
			
			
}
