package org.orienteer.widget;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.OMail;
import org.orienteer.module.ICOFarmModule;
import org.orienteer.module.ICOFarmPerspectiveModule;
import org.orienteer.resource.ICOFarmRegistrationResource;
import org.orienteer.service.IDBService;
import org.orienteer.service.IOMailService;
import org.orienteer.util.EmailExistsValidator;
import org.orienteer.util.ICOFarmUtils;

import java.util.Map;

@Widget(id = ICOFarmPerspectiveModule.REGISTRATION_WIDGET_ID, domain = "browse", selector = ICOFarmModule.REGISTRATION, autoEnable = true)
public class ICOFarmRegistrationWidget extends AbstractICOFarmWidget<OSecurityUser> {

    @Inject
    private IOMailService mailService;

    @Inject
    private IDBService dbService;

    public ICOFarmRegistrationWidget(String id, IModel<OSecurityUser> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Form form = createForm("form");
        TextField<String> emailTextField = new RequiredTextField<>("email", Model.<String>of());
        TextField<String> passwordTextField = new PasswordTextField("password", Model.<String>of());
        TextField<String> reEnterPassword = new PasswordTextField("repeatPassword", Model.<String>of());
        emailTextField.add(EmailAddressValidator.getInstance());
        emailTextField.add(new EmailExistsValidator(false));
        form.add(new EqualPasswordInputValidator(passwordTextField, reEnterPassword));
        form.add(new RequiredTextField<>("firstName", Model.<String>of()));
        form.add(new RequiredTextField<>("lastName", Model.<String>of()));
        form.add(emailTextField);
        form.add(passwordTextField);
        form.add(reEnterPassword);
        form.add(newSubmitLink("submit"));
        form.add(new Label("title", new ResourceModel("application.name")));
        form.add(new Label("content", new ResourceModel("widget.registration.content")));
        add(form);
        add(createSuccessPanel("feedback"));
    }

    private AjaxSubmitLink newSubmitLink(String id) {
        return new AjaxSubmitLink(id) {
            @Override
            @SuppressWarnings("unchecked")
            public void onSubmit(AjaxRequestTarget target) {
                Form<?> form = getForm();
                String firstName = ((TextField<String>) form.get("firstName")).getModelObject();
                String lastName = ((TextField<String>) form.get("lastName")).getModelObject();
                String email = ((TextField<String>) form.get("email")).getModelObject();
                String password = ((TextField<String>) form.get("password")).getModelObject();

                ICOFarmUser user = dbService.createInvestorUser(email, password, firstName, lastName, false);
                updateReferral(user);
                sendActivationEmail(user);

                target.add(ICOFarmRegistrationWidget.this);
            }

            private void updateReferral(ICOFarmUser user) {
                String id = (String) OrienteerWebSession.get().getAttribute("referral");
                ICOFarmUser by = !Strings.isNullOrEmpty(id) ? dbService.getUserBy(ICOFarmUser.ID, id) : null;
                if (by != null) {
                    dbService.updateReferralInformation(user, by);
                }
            }

            private void sendActivationEmail(ICOFarmUser user) {
                Map<Object, Object> macros = ICOFarmUtils.getUserMacros(user);
                String email = user.getEmail();
                OMail oMail = dbService.getMailByName("registration");
                macros.put("link", ICOFarmRegistrationResource.genRegistrationLink(user));
                oMail.setMacros(macros);
                mailService.sendMailAsync(email, oMail);
            }

        };
    }

    private Form<?> createForm(String id) {
        return new Form<Object>(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
            }

            @Override
            protected void onSubmit() {
                super.onSubmit();
                setVisible(false);
                ICOFarmRegistrationWidget.this.get("feedback").setVisible(true);
            }
        };
    }

    private WebMarkupContainer createSuccessPanel(String id) {
        return new WebMarkupContainer(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
                add(new Label("title", new ResourceModel("widget.registration.success.title")));
                add(new Label("content", new ResourceModel("widget.registration.success.content")));
                setVisible(false);
            }
        };
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.user);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.registration.title");
    }

}
