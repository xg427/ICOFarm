package org.orienteer.component.token;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import org.orienteer.service.IDBService;
import org.orienteer.service.ITokenEstimate;
import org.orienteer.service.web3.IEthereumService;
import org.orienteer.util.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.List;

public class BuyTokenPanel extends AbstractTokenPanel {

    private static final Logger LOG = LoggerFactory.getLogger(BuyTokenPanel.class);

	@Inject
	private IEthereumService service;

	@Inject
    private IDBService dbService;

	@Inject
    private ITokenEstimate tokenEstimate;

	public BuyTokenPanel(String id, IModel<Wallet> wallet, IModel<Token> token) {
		super(id, wallet, token);
	}

    @Override
    @SuppressWarnings("unchecked")
    protected void onInitialize(Form<?> form) {
        TextField<String> currencyField = createCurrencyField("currency");
        TextField<String> tokenField = createTokenField("token");

        adjustTokensInputFields(currencyField, tokenField);

        form.add(tokenField);
        form.add(currencyField);
        form.add(createCurrencyDropDown("currencyDropDown"));
    }

    private TextField<String> createCurrencyField(String id) {
	    TextField<String> field = new TextField<>(id, Model.of());
        field.setOutputMarkupId(true);
        return field;
    }

    private TextField<String> createTokenField(String id) {
        return new TextField<String>(id, Model.of()) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupId(true);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(getTokenModel().getObject() != null);
            }
        };
    }

    private void adjustTokensInputFields(TextField<String> weiInputField, TextField<String> tokensInputField) {
        weiInputField.add(createEthFieldEventBehavior(tokensInputField));
	    tokensInputField.add(createTokensFieldEventBehavior(weiInputField));
    }

    private AjaxEventBehavior createEthFieldEventBehavior(TextField<String> tokensField) {
	    return new AjaxFormComponentUpdatingBehavior("input") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                String currencyValue = getCurrencyValue();
                Token token = getTokenModel().getObject();
                Token currency = getCurrency();
                String newToken = computeNullValue(currencyValue, currency, null, token);

                if (!Strings.isNullOrEmpty(newToken)) {
                    tokensField.setModelObject(newToken);
                    target.add(tokensField);
                }
            }
        };
    }

    private AjaxEventBehavior createTokensFieldEventBehavior(TextField<String> currencyField) {
	    return new AjaxFormComponentUpdatingBehavior("input") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                String tokensValue = getTokensValue();
                Token currency = getCurrency();
                Token token = getTokenModel().getObject();
                String newCurrency = computeNullValue(null, currency, tokensValue, token);

                if (!Strings.isNullOrEmpty(newCurrency)) {
                    currencyField.setModelObject(newCurrency);
                    target.add(currencyField);
                }
            }
        };
    }

    private String computeNullValue(String currencyValue, Token currency, String tokenValue, Token token) {
	    String result = "0";
	    if ((!Strings.isNullOrEmpty(currencyValue) || !Strings.isNullOrEmpty(tokenValue)) && (token != null && currency != null)) {
	        try {
	            if (!Strings.isNullOrEmpty(currencyValue)) {
	                result = tokenEstimate.estimateTokens(currencyValue, currency, token).toString();
                } else {
	                result = tokenEstimate.estimateEther(tokenValue, currency, token).toString();
                }
            } catch (NumberFormatException ex) {

            }
        }
	    return result;
    }

    private DropDownChoice<Token> createCurrencyDropDown(String id) {
        List<Token> currencyTokens = dbService.getCurrencyTokens();
        DropDownChoice<Token> choice = new DropDownChoice<>(id, Model.of(currencyTokens.get(0)), currencyTokens,
                ComponentUtils.getChoiceRendererForTokens());
	    choice.setOutputMarkupId(true);
	    choice.setRequired(true);
	    choice.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {}
        });
	    return choice;
    }

    @Override
    protected void onFormSubmit(AjaxRequestTarget target, Form<?> form) {
        Credentials credentials = readCredentials(form);
        if (credentials != null) {
            try {
                buyTokens(credentials);
                onBuyTokens(target);
            } catch (Exception ex) {
                LOG.error("Can't buy tokens!", ex);
                error(new ResourceModel("buy.token.error").getObject());
            }
        }
    }

    private void buyTokens(Credentials credentials) throws Exception {
        Token token = getTokenModel().getObject();
        String tokenAddress = token.getAddress();
        BigInteger gasPrice = token.getGasPrice().toBigInteger();
        BigInteger gasLimit = token.getGasLimit().toBigInteger();
        Convert.Unit unit = Convert.Unit.fromString(getCurrency().getName("en"));
        BigInteger weiValue = Convert.toWei(getCurrencyValue(), unit).toBigInteger();
        service.buyTokens(credentials, tokenAddress, weiValue, gasPrice, gasLimit);// TODO: add state which displays status of buying tokens
    }

    @SuppressWarnings("unchecked")
    private Credentials readCredentials(Form<?> form) {
        String password = ((TextField<String>) form.get("password")).getModelObject();
        Wallet wallet = getWalletModel().getObject();
        try {
            return service.readWallet(password, wallet.getWalletJSON());
        } catch (Exception ex) {
            LOG.error("Password is wrong!", ex);
            error(new ResourceModel("buy.token.wrong.password").getObject());
        }
        return null;
    }

    @Override
    protected void onChangeToken(Token token, AjaxRequestTarget target) {
        target.add(getForm().get("token"));
    }

    protected void onBuyTokens(AjaxRequestTarget target) {

    }

    @Override
    protected List<Token> getTokens(IDBService dbService) {
        return dbService.getTokens(false);
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("buy.token.title");
    }

    @Override
    protected IModel<String> getPasswordLabelModel() {
        return new ResourceModel("buy.token.password");
    }

    @Override
    protected IModel<String> getSelectWalletLabelModel() {
        return new ResourceModel("buy.token.select.wallet");
    }

    @Override
    protected IModel<String> getSelectTokenLabelModel() {
        return new ResourceModel("buy.token.select.token");
    }

    @Override
    protected IModel<String> getSubmitBtnLabelModel() {
        return new ResourceModel("buy.token.submit");
    }

    @SuppressWarnings("unchecked")
    private String getCurrencyValue() {
	    return ((TextField<String>) getForm().get("currency")).getModelObject();
    }

    @SuppressWarnings("unchecked")
    private String getTokensValue() {
	    return ((TextField<String>) getForm().get("token")).getModelObject();
    }

    @SuppressWarnings("unchecked")
    private Token getCurrency() {
	    return ((DropDownChoice<Token>) getForm().get("currencyDropDown")).getModelObject();
    }
}
