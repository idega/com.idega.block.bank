/**
 * @(#)BankAuthenticationServiceImpl.java    1.0.0 9:17:39 AM
 *
 * Idega Software hf. Source Code Licence Agreement x
 *
 * This agreement, made this 10th of February 2006 by and between 
 * Idega Software hf., a business formed and operating under laws 
 * of Iceland, having its principal place of business in Reykjavik, 
 * Iceland, hereinafter after referred to as "Manufacturer" and Agura 
 * IT hereinafter referred to as "Licensee".
 * 1.  License Grant: Upon completion of this agreement, the source 
 *     code that may be made available according to the documentation for 
 *     a particular software product (Software) from Manufacturer 
 *     (Source Code) shall be provided to Licensee, provided that 
 *     (1) funds have been received for payment of the License for Software and 
 *     (2) the appropriate License has been purchased as stated in the 
 *     documentation for Software. As used in this License Agreement, 
 *     Licensee shall also mean the individual using or installing 
 *     the source code together with any individual or entity, including 
 *     but not limited to your employer, on whose behalf you are acting 
 *     in using or installing the Source Code. By completing this agreement, 
 *     Licensee agrees to be bound by the terms and conditions of this Source 
 *     Code License Agreement. This Source Code License Agreement shall 
 *     be an extension of the Software License Agreement for the associated 
 *     product. No additional amendment or modification shall be made 
 *     to this Agreement except in writing signed by Licensee and 
 *     Manufacturer. This Agreement is effective indefinitely and once
 *     completed, cannot be terminated. Manufacturer hereby grants to 
 *     Licensee a non-transferable, worldwide license during the term of 
 *     this Agreement to use the Source Code for the associated product 
 *     purchased. In the event the Software License Agreement to the 
 *     associated product is terminated; (1) Licensee's rights to use 
 *     the Source Code are revoked and (2) Licensee shall destroy all 
 *     copies of the Source Code including any Source Code used in 
 *     Licensee's applications.
 * 2.  License Limitations
 *     2.1 Licensee may not resell, rent, lease or distribute the 
 *         Source Code alone, it shall only be distributed as a 
 *         compiled component of an application.
 *     2.2 Licensee shall protect and keep secure all Source Code 
 *         provided by this this Source Code License Agreement. 
 *         All Source Code provided by this Agreement that is used 
 *         with an application that is distributed or accessible outside
 *         Licensee's organization (including use from the Internet), 
 *         must be protected to the extent that it cannot be easily 
 *         extracted or decompiled.
 *     2.3 The Licensee shall not resell, rent, lease or distribute 
 *         the products created from the Source Code in any way that 
 *         would compete with Idega Software.
 *     2.4 Manufacturer's copyright notices may not be removed from 
 *         the Source Code.
 *     2.5 All modifications on the source code by Licencee must 
 *         be submitted to or provided to Manufacturer.
 * 3.  Copyright: Manufacturer's source code is copyrighted and contains 
 *     proprietary information. Licensee shall not distribute or 
 *     reveal the Source Code to anyone other than the software 
 *     developers of Licensee's organization. Licensee may be held 
 *     legally responsible for any infringement of intellectual property 
 *     rights that is caused or encouraged by Licensee's failure to abide 
 *     by the terms of this Agreement. Licensee may make copies of the 
 *     Source Code provided the copyright and trademark notices are 
 *     reproduced in their entirety on the copy. Manufacturer reserves 
 *     all rights not specifically granted to Licensee.
 *
 * 4.  Warranty & Risks: Although efforts have been made to assure that the 
 *     Source Code is correct, reliable, date compliant, and technically 
 *     accurate, the Source Code is licensed to Licensee as is and without 
 *     warranties as to performance of merchantability, fitness for a 
 *     particular purpose or use, or any other warranties whether 
 *     expressed or implied. Licensee's organization and all users 
 *     of the source code assume all risks when using it. The manufacturers, 
 *     distributors and resellers of the Source Code shall not be liable 
 *     for any consequential, incidental, punitive or special damages 
 *     arising out of the use of or inability to use the source code or 
 *     the provision of or failure to provide support services, even if we 
 *     have been advised of the possibility of such damages. In any case, 
 *     the entire liability under any provision of this agreement shall be 
 *     limited to the greater of the amount actually paid by Licensee for the 
 *     Software or 5.00 USD. No returns will be provided for the associated 
 *     License that was purchased to become eligible to receive the Source 
 *     Code after Licensee receives the source code. 
 */
package com.idega.block.bank.business.impl;

import java.rmi.RemoteException;
import java.util.logging.Level;

import javax.ejb.FinderException;

import org.apache.axis2.AxisFault;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.bank.business.BankAuthenticationService;
import com.idega.block.bank.wsdl.OsifServiceStub;
import com.idega.block.bank.wsdl.OsifServiceStub.GenerateChallengeRequest;
import com.idega.block.bank.wsdl.OsifServiceStub.GenerateChallengeRequestE;
import com.idega.block.bank.wsdl.OsifServiceStub.GenerateChallengeResponse;
import com.idega.block.bank.wsdl.OsifServiceStub.GenerateChallengeResponseE;
import com.idega.block.bank.wsdl.OsifServiceStub.VerifyAuthenticationRequest;
import com.idega.block.bank.wsdl.OsifServiceStub.VerifyAuthenticationRequestE;
import com.idega.block.bank.wsdl.OsifServiceStub.VerifyAuthenticationResponse;
import com.idega.block.bank.wsdl.OsifServiceStub.VerifyAuthenticationResponseE;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.business.DefaultSpringBean;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.CoreUtil;
import com.idega.util.StringUtil;

/**
 * @see BankAuthenticationService
 * <p>You can report about problems to: 
 * <a href="mailto:martynas@idega.is">Martynas Stakė</a></p>
 *
 * @version 1.0.0 Jun 21, 2013
 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
 */
@Service(BankAuthenticationService.BEAN_NAME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class BankAuthenticationServiceImpl extends DefaultSpringBean implements BankAuthenticationService {
	
private UserBusiness userBusiness = null;
	
	private OsifServiceStub stub = null;
	
	@Override
	public String generateChallenge(
			String policy, int provider, String transactionId) { 
		GenerateChallengeRequest generateChallengeRequest = null;
		generateChallengeRequest = getInstantiatedObject(
				GenerateChallengeRequest.class);
		generateChallengeRequest.setPolicy(policy);
		generateChallengeRequest.setProvider(provider);
		generateChallengeRequest.setTransactionID(transactionId);

		GenerateChallengeRequestE generateChallengeRequestE = null;
		generateChallengeRequestE = getInstantiatedObject(
				GenerateChallengeRequestE.class);
		generateChallengeRequestE.setGenerateChallengeRequest(generateChallengeRequest);
		
		GenerateChallengeResponseE generateChallengeResponseE = null;
		try {
			generateChallengeResponseE = getOsifServiceStub()
					.generateChallenge(generateChallengeRequestE);
		} catch (RemoteException e) {
			getLogger().log(Level.WARNING, 
					"Unable to connect OSIF service provider cause of: ", e);
		}
		
		GenerateChallengeResponse generateChallengeResponse = null;
		generateChallengeResponse = generateChallengeResponseE
				.getGenerateChallengeResponse();
		if (generateChallengeResponse == null) {
			getLogger().warning(
					"Failed to get " + GenerateChallengeResponse.class);
			return null;
		}
		
		return generateChallengeResponse.getChallenge();
	}
	
	@Override
	public User verifyAuthentication(	String hiddenTbs,
			String host, String policy, int provider, String signature,
			String transactionId) {
		
		String personalId = verifyAuthentication(
				generateChallenge(policy, provider, transactionId), 
				hiddenTbs, host, policy, provider, signature, transactionId);
		if (StringUtil.isEmpty(personalId)) {
			return null;
		}
		
		return getUser(personalId);
	}
	
	@Override
	public String verifyAuthentication(	String challenge, String hiddenTbs,
			String host, String policy, int provider, String signature,
			String transactionId) {
		VerifyAuthenticationRequest verifyAuthenticationRequest = null;
		verifyAuthenticationRequest = getInstantiatedObject(
				VerifyAuthenticationRequest.class);
		verifyAuthenticationRequest.setChallenge(challenge);
		verifyAuthenticationRequest.setHiddenTbs(hiddenTbs);
		verifyAuthenticationRequest.setHost(host);
		verifyAuthenticationRequest.setPolicy(policy);
		verifyAuthenticationRequest.setProvider(provider);
		verifyAuthenticationRequest.setSignature(signature);
		verifyAuthenticationRequest.setTransactionID(transactionId);
		
		VerifyAuthenticationRequestE verifyAuthenticationRequestE = null;
		verifyAuthenticationRequestE = getInstantiatedObject(
				VerifyAuthenticationRequestE.class);
		verifyAuthenticationRequestE.setVerifyAuthenticationRequest(
				verifyAuthenticationRequest);
		
		VerifyAuthenticationResponseE verifyAuthenticationResponseE = null;
		try {
			verifyAuthenticationResponseE = getOsifServiceStub()
					.verifyAuthentication(verifyAuthenticationRequestE);
		} catch (RemoteException e) {
			getLogger().log(Level.WARNING, 
					"Unable to connect OSIF service provider cause of: ", e);
		}
		
		VerifyAuthenticationResponse verifyAuthenticationResponse = null;
		verifyAuthenticationResponse = verifyAuthenticationResponseE
				.getVerifyAuthenticationResponse();
		if (verifyAuthenticationResponse == null) {
			getLogger().warning(VerifyAuthenticationResponse.class + 
					" not found!");
			return null;
		}
		
		return verifyAuthenticationResponse.getUserID();
	}
	
	protected static <T> T getInstantiatedObject(
			java.lang.Class<T> type) {
		if (type == null) {
			return null;
		}
		
		try {
			return type.newInstance();
		} catch (InstantiationException e) {
			getLogger(BankAuthenticationService.class).log(Level.WARNING, 
					"Unable to instantiate " + type + " cause of: ", e);
		} catch (IllegalAccessException e) {
			getLogger(BankAuthenticationService.class).log(Level.WARNING, 
					"Can't access constructor of " + type + " cause of: ", e);
		}
		
		return null;
	}
	
	protected OsifServiceStub getOsifServiceStub() {
		if (this.stub == null) {
			try {
				this.stub = new OsifServiceStub();
			} catch (AxisFault e) {
				getLogger().log(Level.WARNING, "Unable to create axis service stub:", e);
			}
		}
		
		return this.stub;
	}
	
	protected User getUser(String personalId) {
		if (StringUtil.isEmpty(personalId)) {
			return null;
		}
		
		try {
			return getUserBusiness().getUser(personalId);
		} catch (RemoteException e) {
			getLogger().log(Level.WARNING, "Unable to connect database: ", e);
		} catch (FinderException e) {
			getLogger().log(Level.WARNING, "Unable to find " + User.class + 
					" by personal id: " + personalId);
		}
		
		try {
			return getUserBusiness().getUser(Integer.valueOf(personalId));
		} catch (NumberFormatException e) {
			getLogger().log(Level.WARNING,
					"Unable to convert id: " + personalId + " to numeric.");
		} catch (RemoteException e) {
			getLogger().log(Level.WARNING, "Unable to connect data source: ", e);
		}
		
		return null;
	}
	
	protected UserBusiness getUserBusiness() {
		if (this.userBusiness != null) {
			return this.userBusiness;
		}

		try {
			this.userBusiness = IBOLookup.getServiceInstance(
					CoreUtil.getIWContext(), UserBusiness.class);
		} catch (IBOLookupException e) {
			getLogger().log(Level.WARNING, 
					"Unable to get: " + UserBusiness.class + ": ", e);
		}

		return this.userBusiness;
	}
}
