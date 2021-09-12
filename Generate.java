public class Generate extends AbstractGenerate {
    public Generate(){}
    /**
     * Method reporting error messages and throwing Compilation Exceptions
     */
    @Override
    public void reportError(Token token, String explanatoryMessage) throws CompilationException{

        System.out.println("Error at line: "+ token.lineNumber + " Found " + token.text + "  " + explanatoryMessage);
        
        throw new CompilationException("Error with token " + token.text + "at line: " + token.lineNumber);
    }


}
