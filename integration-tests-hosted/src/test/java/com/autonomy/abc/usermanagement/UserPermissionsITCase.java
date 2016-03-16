package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.hsod.HSODApplication;
import com.autonomy.abc.selenium.hsod.HSODElementFactory;
import com.autonomy.abc.selenium.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.users.*;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.sso.GoogleAuth;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

@RelatedTo("HOD-532")
public class UserPermissionsITCase extends HostedTestBase {
    public UserPermissionsITCase(TestConfig config) {
        super(config);
    }

    private UserService userService;
    private User user;

    private Session devSession;

    private Session userSession;
    private HSODApplication userApp;
    private HSODElementFactory userElementFactory;

    @Before
    public void setUp(){
        userService = getApplication().userService();
        GoogleAuth googleAuth = (GoogleAuth) userService.createNewUser(getConfig().generateNewUser(), Role.ADMIN).getAuthProvider();

        user = userService.createNewUser(getConfig().getNewUser("newhppassport"), Role.ADMIN);

        devSession = getMainSession();
        userApp = new HSODApplication();
        userSession = launchInNewSession(userApp);
        userElementFactory = userApp.elementFactory();

        new AuthenticatesUsers(getConfig().getWebDriverFactory(), getConfig().getAuthStrategy()).authenticate(user);

        try {
            userApp.loginService().login(user);
        } catch (NoSuchElementException e) {
            assumeThat("Authentication failed", userSession.getDriver().getPageSource(), not(containsString("Authentication Failed")));
            assumeThat("Promotions page not displayed", userApp.elementFactory().getPromotionsPage(), displayed());
        }
    }

    @After
    public void tearDown(){
        userService.deleteOtherUsers();
        getConfig().getAuthStrategy().cleanUp(getDriver());
    }

    @Test
    public void testCannotNavigate(){
        verifyThat(userElementFactory.getPromotionsPage(), displayed());

        userService.deleteUser(user);

        try {
            userApp.switchTo(AnalyticsPage.class);
        } catch (StaleElementReferenceException | NoSuchElementException | TimeoutException e){
            //Expected as you'll be logged out
        }

        new WebDriverWait(userSession.getDriver(), 10).until(ExpectedConditions.titleIs("Haven Search OnDemand - Error"));

        verifyAuthFailed();
    }

    @Test
    public void testCannotAddKeywords(){
        String blacklist = "Dave";

        KeywordService keywordService = userApp.keywordService();
        KeywordsPage keywordsPage = keywordService.goToKeywords();
        keywordsPage.createNewKeywordsButton().click();
        CreateNewKeywordsPage createNewKeywordsPage = userElementFactory.getCreateNewKeywordsPage();

        Waits.loadOrFadeWait();

        userService.deleteUser(user);

        try {
            createNewKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
            createNewKeywordsPage.continueWizardButton().click();
            createNewKeywordsPage.getTriggerForm().addTrigger(blacklist);
            createNewKeywordsPage.finishWizardButton().click();

            verifyError();
        } catch (TimeoutException e) {
            try {
                verifyError();
            } catch (Exception f) {
                verifyAuthFailed();
            }
        } catch (StaleElementReferenceException e) {
            Waits.loadOrFadeWait();
            verifyAuthFailed();
        }

        getApplication().switchTo(KeywordsPage.class);

        verifyThat(getElementFactory().getKeywordsPage().getBlacklistedTerms(), not(hasItem(blacklist)));
    }

    @Test
    public void testCannotAddPromotions(){
        PromotionService promotionService = userApp.promotionService();
        userApp.switchTo(SearchPage.class);

        userService.deleteUser(user);

        try {
            promotionService.setUpPromotion(new SpotlightPromotion("BE ALONE"), "Baggins", 2);

            verifyError();
        } catch (StaleElementReferenceException | TimeoutException e) {
            verifyThat(userSession.getDriver().getPageSource(), anyOf(containsString("Authentication Failed"), containsString("Error executing search action")));
        }
    }

    @Test
    public void testCannotAddStaticPromotion(){
        HSODPromotionService promotionService = userApp.promotionService();
        HSODPromotionsPage promotionsPage = promotionService.goToPromotions();

        promotionsPage.staticPromotionButton().click();
        HSODCreateNewPromotionsPage createNewPromotionsPage = userElementFactory.getCreateNewPromotionsPage();

        userService.deleteUser(user);

        try {
            new StaticPromotion("TITLE", "CONTENT", "TRIGGER").makeWizard(createNewPromotionsPage).apply();

            verifyError();
        } catch (StaleElementReferenceException e) {
            verifyAuthFailed();
        }
    }

    @Test
    public void testCannotAddUser(){
        UserService userUserService = userApp.userService();

        UsersPage usersPage = userUserService.goToUsers();

        userService.deleteUser(user);

        try {
            usersPage.createUserButton().click();
            try {
                usersPage.addNewUser(getConfig().generateNewUser(), Role.ADMIN);
            } finally {
                usersPage.closeModal();
            }

           verifyError();
        } catch (NoSuchElementException | StaleElementReferenceException | TimeoutException e) {
            verifyAuthFailed();
        }
    }

    private void verifyError(){
        TopNavBar topNavBar = userElementFactory.getTopNavBar();
        topNavBar.openNotifications();
        NotificationsDropDown notificationsDropDown = topNavBar.getNotifications();
        verifyThat(notificationsDropDown.getNotification(1).getMessage(), containsString("Error"));
    }

    private void verifyAuthFailed() {
        verifyThat(userSession.getDriver().getPageSource(), containsString("Authentication Failed"));
    }
}
