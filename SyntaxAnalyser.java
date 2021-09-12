import java.io.*;


public class SyntaxAnalyser extends AbstractSyntaxAnalyser {
    
    private String fileName; // the name of the current file

    public SyntaxAnalyser(String fileName) {
        this.fileName = fileName;
        try {
            lex = new LexicalAnalyser(fileName); // Lexical Analyser
        } catch (Exception e) {
            System.out.println("Error at LexicalAnalyser");
        }
    }

    /** Accept a token based on context.
     * @param symbol
     * @throws IOException
     * @throws CompilationException
     */
    @Override
    public void acceptTerminal(int symbol) throws IOException, CompilationException {

        if (nextToken.symbol == symbol) {
            myGenerate.insertTerminal(nextToken);
            nextToken = lex.getNextToken();
            return;
        }
        System.out.println("Error at:  " + this.fileName );
        myGenerate.reportError(nextToken, "Expected: " + Token.getName(symbol));
    }

    /**
	 * Processing non-terminal statements
     * 
	 * @throws IOException
     * @throws CompilationException
     */
    private void statement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Statement"); // starting to read statements
        try {
            switch (nextToken.symbol) {
                //call statement
                case Token.ifSymbol:                  
                    ifStatement();

                    break;
                    //while statement
                case Token.whileSymbol: 					
                     whileStatement();

                    break;
                    //assignment statement
                case Token.identifier:					
                    assigmentStatement();

                    break;
                    //call statement
                case Token.callSymbol:					
                    callstatement();

                    break;
                    // for statement
                case Token.forSymbol:					
                    forStatement();

                    break;
                    // until statement
                case Token.untilSymbol:						
                    untilStatement();

                    break;    
                    // Default checking for errors
                default:  
                                              
                    myGenerate.reportError(nextToken, "Error, expected statement");

                    break;

            }
        } catch (CompilationException e) 
        {
            // Error handeling 
            myGenerate.reportError(nextToken, "Invalid statement");

        }
		
        myGenerate.finishNonterminal("Statement"); // end of the reading 
    }

    /**
     * Statement list going through the statements
	 * @throws IOException
     * @throws CompilationException
     */
    private void statementList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("StatementList"); // begining of the reading
        try {
            statement();  // loading the statement
        } catch (CompilationException e) {
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at statement list");
        }
	
        while (nextToken.symbol == Token.semicolonSymbol) { // check if the next symbol is a semicolon - enters recursion
            acceptTerminal(Token.semicolonSymbol);
            try {
                statementList();
            } catch (CompilationException e) {
                // Error handeling 
                myGenerate.reportError(nextToken, "Error at statement list");
            }
        }
        myGenerate.finishNonterminal("StatementList"); // exiting statement List
    }

    /**
	 * The first level of the file starts parsing, finds the first begin, enters statement List
     * and stops when it finds the end symbol of the file
     * @throws IOException
     * @throws CompilationException
     */
    @Override
    public void _statementPart_() throws IOException, CompilationException {
		try {
			acceptTerminal(Token.beginSymbol);
            myGenerate.commenceNonterminal("StatementPart");
            statementList(); 
        } catch (CompilationException e) {
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at statement list");
        }
        myGenerate.finishNonterminal("StatementPart");
        acceptTerminal(Token.endSymbol);
        
    }

	
    /**
	 * Begins to read the assigment Statement, acepts identifier and := symbol
     * enters expression and exits
     * 
	 * @throws IOException
     * @throws CompilationException
     */	
    private void assigmentStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("AssigmentStatement");
		acceptTerminal(Token.identifier);
        acceptTerminal(Token.becomesSymbol);
        if (nextToken.symbol == Token.stringConstant) {
            acceptTerminal(Token.stringConstant);
            myGenerate.finishNonterminal("AssigmentStatement");
            return;
        }else{
		
		 try {
            expression();
         } catch (CompilationException e) {
             // Error handeling 
            myGenerate.reportError(nextToken, "Error on expression");
         }
		}
		//Finishes reading assigmentStatement
        myGenerate.finishNonterminal("AssigmentStatement");
    }

    /**
     * If statement 
     * starts the condition
     * checks for else symbol and enters statement list
     * exits when reaches end symbol
     * @throws IOException
     * @throws CompilationException
     */ 
    private void ifStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("IFStatement");

        acceptTerminal(Token.ifSymbol);
        try {
            condition();
        } catch (CompilationException e) {
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at condition");
        }
        acceptTerminal(Token.thenSymbol);
        
        try {
            statementList();

            if (nextToken.symbol == Token.elseSymbol) {
                
                acceptTerminal(Token.elseSymbol);
                statementList();
            }
        } catch (CompilationException e) {
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at statement list");
        }
        
        acceptTerminal(Token.endSymbol);
        acceptTerminal(Token.ifSymbol);
        myGenerate.finishNonterminal("IFStatement");
    }

    /**
     * For Statement
     * 
     * @throws IOException
     * @throws CompilationException
     */ 
    private void forStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ForStatement");
        
        acceptTerminal(Token.forSymbol);
        acceptTerminal(Token.leftParenthesis);
        try{    

         assigmentStatement();  
         acceptTerminal(Token.semicolonSymbol);
         condition();
         acceptTerminal(Token.semicolonSymbol);
         assigmentStatement();
         
        } catch (CompilationException e) {
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at statement list");

        }  

        acceptTerminal(Token.rightParenthesis);
  
        acceptTerminal(Token.doSymbol);
        try {

            statementList();

        } catch (CompilationException e) {
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at statement list");

        }
        
        acceptTerminal(Token.endSymbol);
        acceptTerminal(Token.loopSymbol);
        myGenerate.finishNonterminal("ForStatement");
    }

    /**
     * While statement
     * enters condition and statment list
     * exits when reaches end/loop symbol
     * 
     * 
     * @throws IOException
     * @throws CompilationException
     */ 
    private void whileStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("WhileStatement");
       
        acceptTerminal(Token.whileSymbol);
        try {
            condition();
        } catch (CompilationException e) {
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at condition");
        }
        acceptTerminal(Token.loopSymbol);

        try {
            statementList();
        } catch (CompilationException e) {
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at statement list");
        }

        acceptTerminal(Token.endSymbol);
        acceptTerminal(Token.loopSymbol);

        myGenerate.finishNonterminal("WhileStatement");
    }

    /**
     * Until Statement
     * 
     * @throws IOException
     * @throws CompilationException
     */ 
    private void untilStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("UntilStatement");
        acceptTerminal(Token.doSymbol);
        try {
            
            statementList();

        } catch (CompilationException e) {
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at statement list");
        }

        acceptTerminal(Token.untilSymbol);

        try {

            condition();

        } catch (CompilationException e) {
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at condition");

        }
        myGenerate.finishNonterminal("UntilStatement");
    }

    /**
     * Call statement
     * begins reading call symbol and identifier
     * enters argument list 
     * 
     * 
     * @throws IOException
     * @throws CompilationException
     */ 
    private void callstatement() throws IOException, CompilationException {
    
        myGenerate.commenceNonterminal("CallStatement");
        acceptTerminal(Token.callSymbol);
        acceptTerminal(Token.identifier);
    
        acceptTerminal(Token.leftParenthesis);
        try {
            argumentList();
        } catch (CompilationException e) {
            myGenerate.reportError(nextToken, "Error at argument list");
        }
        acceptTerminal(Token.rightParenthesis);
        myGenerate.finishNonterminal("CallStatement");
    }


    
    /**
    * Condition
    * 
    * @throws IOException
    * @throws CompilationException
    */ 
    private void condition() throws IOException, CompilationException {
        // Starts reading condition
        myGenerate.commenceNonterminal("Condition");
        acceptTerminal(Token.identifier);
        try {

            operator();

            switch (nextToken.symbol) {

                case Token.identifier:
                acceptTerminal(Token.identifier);
                break;

                case Token.stringConstant:
                acceptTerminal(Token.stringConstant);
                break;

                case Token.numberConstant:
                acceptTerminal(Token.numberConstant);
                break;


                default:

                System.out.println("Invalid identifier");
            }
        } catch (CompilationException e) {
            
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at codition");
        }
        myGenerate.finishNonterminal("Condition");
    }

     /**
     * Argument List
     * checks whnen it reaches comma and enters recursion
     * @throws IOException
     * @throws CompilationException
     */ 
    private void argumentList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ArgumentList");
        acceptTerminal(Token.identifier);

        try {
            
            if(nextToken.symbol == Token.commaSymbol) 
            {

                acceptTerminal(Token.commaSymbol);
                argumentList();

            }
        } catch (CompilationException e) {
            // Error handeling 
            myGenerate.reportError(nextToken, "Error at Argument List");

        }
        myGenerate.finishNonterminal("ArgumentList");
    }

    

    /**
	 * Expressions
     * cheks for the next token plus or minus, accepts it and enters recursion
	 * @throws IOException
     * @throws CompilationException
     */	
	
    private void expression() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Expression");

        try {
            term();
            if(plus(nextToken) || minus(nextToken))
            {
		
			    acceptTerminal(nextToken.symbol);
                expression();

            }

        } catch (CompilationException e) {

            //Error handeling
            myGenerate.reportError(nextToken, "Error at Expression");

        }
        myGenerate.finishNonterminal("Expression");
    }

    /**
     * Checking for less, greaterthan lessequal equal and not equal symbols
     * 
     * @throws IOException
     * @throws CompilationException
     */ 
    private void operator() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ConditionalOperator");
        switch (nextToken.symbol) {

            case Token.lessThanSymbol:
            acceptTerminal(Token.lessThanSymbol);
            break;

            case Token.notEqualSymbol:
            acceptTerminal(Token.notEqualSymbol);
            break;

            case Token.lessEqualSymbol:
            acceptTerminal(Token.lessEqualSymbol);
            break;

            case Token.greaterEqualSymbol:
            acceptTerminal(Token.greaterEqualSymbol);
            break;

            case Token.greaterThanSymbol:
            acceptTerminal(Token.greaterThanSymbol);
            break;

            case Token.equalSymbol:
            acceptTerminal(Token.equalSymbol);
            break;
            

            default:
        }
        myGenerate.finishNonterminal("ConditionalOperator");
    }

    
    /**
     * Check for plus symbol
     * @param token
     * @return
     */
    private boolean plus(Token token)
    {
        return token.symbol == Token.plusSymbol;
    }

    /**
     * Cheks for minus symbol
     * @param token
     * @return
     */
    private boolean minus(Token token)
    {
        return token.symbol == Token.minusSymbol;
    }

    /**
     * return times symbol
     *
     * @return boolean
     */
    private boolean times(Token token) {
        return  token.symbol == Token.timesSymbol;
    }

    /**
     * return divide symbol
     * @param token
     * @return
     */
    private boolean divide(Token token)
    {
        return token.symbol == Token.divideSymbol;
    }


    /**
	 * Factor
     * Checks for identifier or number constant/expression
     * accepts it and exits
	 * @throws IOException
     * @throws CompilationException
     */	
    private void factor() throws IOException, CompilationException {
		// Starts reading factor
        myGenerate.commenceNonterminal("Factor");
        try {
			// Can switch to identifier or numberConstant or (expression)
            switch (nextToken.symbol)
             {

                case Token.leftParenthesis:
                acceptTerminal(Token.leftParenthesis);
                expression();
                acceptTerminal(Token.rightParenthesis);

                case Token.identifier:
                acceptTerminal(Token.identifier);
                break;

                case Token.numberConstant:
				acceptTerminal(Token.numberConstant);	
				break;
                
                default:
                //Error Handeling
                myGenerate.reportError(nextToken, "Error at factor");
                break;
            }

        } catch (CompilationException e) {
            // Error Handeling
            myGenerate.reportError(nextToken, "Error at Factor");
        }
        myGenerate.finishNonterminal("Factor");
    }

    /**
	 * Term
     * Enters Factor and checks for * or / operator, acepts the symbol
     * enters recursion
     * 
	 * @throws IOException
     * @throws CompilationException
     */	
    private void term() throws IOException, CompilationException {
	    // Starts reading term
        myGenerate.commenceNonterminal("Term");

        try {
            factor();
	
            while (times(nextToken) || divide(nextToken)) 
            {
                acceptTerminal(nextToken.symbol);
                term();

            }
        } catch (CompilationException e) {
            // Erro handeling
            myGenerate.reportError(nextToken, "Error at Term");
        }
        myGenerate.finishNonterminal("Term");
    }
	
	
    
	
}

