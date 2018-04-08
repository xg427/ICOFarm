package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class TokenCurrency extends Currency{
	private static final long serialVersionUID = 1L;

	public TokenCurrency(ODocument currencyDoc) {
		super(currencyDoc);
	}

	public String getContractAddress(){
		return getDocument().field("token.contractAddress");
	}
}