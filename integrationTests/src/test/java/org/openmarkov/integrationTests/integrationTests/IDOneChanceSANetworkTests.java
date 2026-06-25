package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.BeforeEach;

import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader_1_0;

import java.io.IOException;

public class IDOneChanceSANetworkTests extends IDNetworkTests {
	
	@Override
	@BeforeEach public void setUp() throws java.net.URISyntaxException, ProbNetParserException, IOException {
		networkName = "networks/id/ID-one-chance-sa.pgmx";
		super.setUp();
	}

	@Override
	protected PGMXReader_1_0 newPGMXReader() {
		// TODO Auto-generated method stub
		return new PGMXReader_1_0();
	}

}
