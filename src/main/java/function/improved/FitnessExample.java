package function.improved;

import fitnesse.responders.run.SuiteResponder;
import fitnesse.wiki.*;

import java.util.HashMap;
import java.util.Map;

public class FitnessExample {

    public String testableHtml(PageData pageData, boolean includeSuiteSetup) throws Exception {
        return new SetUpTearDownSurrounder(pageData, includeSuiteSetup).getTestHtmlWithSetUpAndTearDown();
    }

    private class SetUpTearDownSurrounder {
        private Map<String, String> prefixByResponder = new HashMap<String, String>();
        private PageData pageData;
        private boolean includeSuiteSetup;
        private WikiPage wikiPage;

        public SetUpTearDownSurrounder(PageData pageData, boolean includeSuiteSetup) {
            this.pageData = pageData;
            this.includeSuiteSetup = includeSuiteSetup;
            wikiPage = pageData.getWikiPage();

            init();
        }

        public void init() {
            prefixByResponder.put(SuiteResponder.SUITE_SETUP_NAME, "!include -setup .");
            prefixByResponder.put("Setup", "!include -setup .");
            prefixByResponder.put(SuiteResponder.SUITE_TEARDOWN_NAME, "!include -teardown .");
            prefixByResponder.put("TearDown", "!include -teardown .");
        }

        public String getTestHtmlWithSetUpAndTearDown() throws Exception {
            final WikiPage wikiPage = this.pageData.getWikiPage();
            final String content = getTestContentWithSetUpAndTearDown();
            this.pageData.setContent(content);

            return this.pageData.getHtml();
        }

        public String getTestContentWithSetUpAndTearDown() throws Exception {
            StringBuffer buffer = new StringBuffer();
            buffer.append(getSetUpTestPageForRender());
            buffer.append(this.pageData.getContent());
            buffer.append(getTearDownTestPageForRender());

            return buffer.toString();
        }

        public String getSetUpTestPageForRender() throws Exception {
            StringBuffer buffer = new StringBuffer();
            if (isTestPage()) {
                if (this.includeSuiteSetup) {
                    buffer.append(getPageForRender(SuiteResponder.SUITE_SETUP_NAME));
                }
                buffer.append(getPageForRender("SetUp"));
            }

            return buffer.toString();
        }
        public String getTearDownTestPageForRender() throws Exception {
            StringBuffer buffer = new StringBuffer();
            if (isTestPage()) {
                buffer.append(getPageForRender("TearDown"));
                if (this.includeSuiteSetup) {
                    buffer.append(getPageForRender(SuiteResponder.SUITE_TEARDOWN_NAME));
                }
            }
            return buffer.toString();
        }

        public boolean isTestPage() throws Exception {
            return this.pageData.hasAttribute("Test");
        }

        public String getPageForRender(String pageName) throws Exception {
            WikiPage suiteSetup = PageCrawlerImpl.getInheritedPage(pageName, this.wikiPage);
            if (suiteSetup != null) {
                WikiPagePath pagePath = this.wikiPage.getPageCrawler().getFullPath(suiteSetup);
                String pagePathName = PathParser.render(pagePath);
                return String.format("%s%s\n", prefixByResponder.get(pageName), pagePathName);
            }

            return "";
        }
    }

}