package wci.frontend.pascal.parsers;

import java.util.EnumSet;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.*;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;


/**
 * <h1>WhenStatementParser</h1>
 *
 * <p>
 * Parse a Made-up Pascal WHEN statement.
 * </p>
 *
 * <p>
 * Copyright (c) 2009 by Ronald Mak
 * </p>
 * <p>
 * For instructional purposes only. No warranties.
 * </p>
 */
public class WhenStatementParser extends StatementParser {
	/**
	 * Constructor.
	 * 
	 * @param parent the parent parser.
	 */
	public WhenStatementParser(PascalParserTD parent) {
		super(parent);
	}

	// Synchronization set for NEXT.
	private static final EnumSet<PascalTokenType> NEXT_SET = StatementParser.STMT_START_SET.clone();
	static {
		NEXT_SET.add(NEXT);
		NEXT_SET.addAll(StatementParser.STMT_FOLLOW_SET);
	}

	/**
	 * Parse an WHEN statement.
	 * 
	 * @param token the initial token.
	 * @return the root node of the generated parse tree.
	 * @throws Exception if an error occurred.
	 */
	public ICodeNode parse(Token token) throws Exception {

		ExpressionParser expressionParser = new ExpressionParser(this);
		StatementParser statementParser = new StatementParser(this);
		// consume the WHEN
		token = nextToken();
		// Create an IF node.
		ICodeNode ifNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.IF);

		// save the reference to the root IF node
		ICodeNode rootIfNode = ifNode;

		while (true) {
			// Parse the conditional expression before =>
			// The IF node adopts the expression subtree as its first child.

			ifNode.addChild(expressionParser.parse(token));

			token = currentToken();
			// consume the =>
			if (token.getType() == NEXT) {
				token = nextToken();
			} else {
				errorHandler.flag(token, MISSING_NEXT, this);
			}

			// Parse the statement after =>
			// The IF node adopts the statement subtree as its second child.
			ifNode.addChild(statementParser.parse(token));

			token = currentToken();
			// consume the ;
			if (token.getType() == SEMICOLON) {
				token = nextToken();
			} else {
				errorHandler.flag(token, MISSING_SEMICOLON, this);
			}

			// Look if there is an OTHERWISE.
			if (token.getType() == OTHERWISE) {

				// consume the OTHERWISE
				token = nextToken();

				// consume the => in OTHERWISE clause
				if (token.getType() == NEXT) {
					token = nextToken();
				} else {
					errorHandler.flag(token, MISSING_NEXT, this);
				}
				// parse the statement after =>
				// The IF node adopts the statement subtree as its third child.
				ifNode.addChild(statementParser.parse(token));
				token = currentToken();
				// consume the END
				if (token.getType() == END) {
					token = nextToken();
				} else {
					errorHandler.flag(token, MISSING_END, this);
				}
				break;
				// 'OTHERWISE' not found
			} else {
				// Create an new IF node to be adopted as the third child of parent IF node

				ICodeNode childIfNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.IF);
				ifNode.addChild(childIfNode);
				ifNode = childIfNode;
			}
		}
		return rootIfNode;
	}
}
