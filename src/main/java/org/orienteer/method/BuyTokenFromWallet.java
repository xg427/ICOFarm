package org.orienteer.method;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.ODocumentFilter;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.method.filter.ODocumentExistsFilter;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;

@OMethod(
		order = 1,
		icon = FAIconType.money,
		titleKey = "method.token.buy.fromWallet",
		bootstrap = BootstrapType.SUCCESS,
		filters = {
			@OFilter(fClass = ODocumentFilter.class, fData = Wallet.CLASS_NAME),
			@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
			@OFilter(fClass = ODocumentExistsFilter.class, fData = "true")
		}
)
public class BuyTokenFromWallet extends AbstractBuyTokenMethod {

	@Override
	@SuppressWarnings("unchecked")
	protected Wallet getWallet() {
		IModel<ODocument> docModel = (IModel<ODocument>) getContext().getDisplayObjectModel();
		return new Wallet(docModel.getObject());
	}

	@Override
	protected Token getToken() {
		return null;
	}

}
