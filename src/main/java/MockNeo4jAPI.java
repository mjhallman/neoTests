
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.query.QueryEngine;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

import java.util.*;

/**
 * User: Matt Hallman
 * Date: 2/26/14
 * Time: 4:32 PM
 */
public class MockNeo4jAPI implements MockAPI {


    RestAPI restAPI = new RestAPIFacade("http://localhost:7474/db/data");

    public void clearDB() {
        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        engine.query(String.format("start r=relationship(*) delete r;"), Collections.emptyMap());
        engine.query(String.format("start r=node(*) delete r;"), Collections.emptyMap());
    }

    @Override
    public void createUser(int id, String username) {
        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        engine.query(String.format("CREATE (u:User {id:%d, username:'%s'});", id, username), Collections.emptyMap());
    }

    @Override
    public void createDoc(long ts, int userId, int docId, String docType) {
        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        engine.query(String.format("MATCH (u:User {id:%d}) CREATE (u)<-[:BY_USER]-(c:Event:Create {ts:%d})-[:ON_DOC]->(d:Doc:%s {id:%d})", userId, ts, docType, docId), Collections.emptyMap());

    }

    @Override
    public void bulkCreate(List<MockAPI.CreateDoc> creates) {
//        StringBuilder builder = new StringBuilder();
//        CreateDoc firstCreate = creates.get(0);
//        builder.append(String.format("Match (u:User {id:%d}) CREATE (u)-[:CREATED {ts:%d}]->(d:Doc {id:%d});", firstCreate.userId, firstCreate.ts, firstCreate.docId));

    }

    @Override
    public void createReference(int mapId, int references, String referenceType) {

        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        String query = String.format("Match (parent:Doc {id:%d}), " +
                "(child:Doc {id:%d}) " +
                "CREATE (parent)-[:HAS_REFERENCE {type:'%s'}]->(child);", mapId, references, referenceType);
//        System.out.println(query);
        engine.query(query, Collections.emptyMap());
    }

    @Override
    public void editDoc(long ts, int userId, int docId) {
        String query = String.format("MATCH (u:User {id:%d}), (d:Doc {id:%d}) CREATE (u)<-[:BY_USER]-(e:Event:Edit {ts:%d})-[:ON_DOC]->d;", userId, docId, ts);
        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        engine.query(query, Collections.emptyMap());

    }

    @Override
    public void createReviewRequest(long ts, int reviewId, int userId, int docToReview, List<Integer> reviewers) {

        String query = String.format("MATCH (u:User {id:%d}), " +
                "(d:Doc {id:%d}), " +
//                                     "(d)-[:HAS_REFERENCE*]->(child:Doc)," +
                "(reviewer:User) where reviewer.id in %s " +
                "CREATE UNIQUE  (rr:Event:ReviewRequest {id:%d, ts:%d})-[:ON_DOC]->(d) " +
                "CREATE UNIQUE (rr)-[:BY_USER]->(u) " +
                "CREATE UNIQUE (rr)-[:FOR_REVIEWER]->reviewer;",
                userId, docToReview, reviewers, reviewId, ts);
        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        engine.query(query, Collections.emptyMap());

    }

    @Override
    public void approveDocument(MockAPI.Approval approval) {
        String query = String.format("MATCH (u:User {id:%d}), (rr:ReviewRequest {id:%d}), (d:Doc {id:%d}) " +
                "CREATE (e:Event:Approve {ts:%d}), " +
                "(e)-[:BY_USER]->(u), (e)-[:ON_DOC]->(d), " +
                "(e)-[:FOR_REQUEST]->(rr)", approval.userId, approval.reviewId, approval.docId, approval.ts );




//                "MATCH (user:User {id:%d})<-[rel:NEEDS_REVIEW {reviewId:%d}]-(approved:Doc {id:%d})" +
//                                     "DELETE rel " +
//                                     "CREATE (user)-[:APPROVED {reviewId:%d, ts:%d}]->approved;", approval.userId, approval.reviewId, approval.docId, approval.reviewId, approval.ts);
//        System.out.println(query);
        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        engine.query(query, Collections.emptyMap());
    }

    // Who hasn’t completed review for request X?
    @Override
    public List<Integer> getReviewersNotCompleted(int reviewRequest) {
        String query = String.format("MATCH (rr:ReviewRequest {id:%d})-[:FOR_REVIEWER]->(reviewer:User), " +
                "(rr)-[:ON_DOC]->(parent:Doc)-[:HAS_REFERENCE*]->(child:Doc) " +
                "OPTIONAL MATCH (rr)<-[:FOR_REQUEST]-(a:Approve)-[:BY_USER]->(reviewer) " +
                "WITH reviewer.id as reviewerId, " +
                "count(distinct a) as approvalsPerformed, " +
                "count(distinct child) as approvalsNeeded  " +
                "WHERE approvalsPerformed < approvalsNeeded RETURN reviewerId",
                reviewRequest);
        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        QueryResult result = engine.query(query, Collections.emptyMap());
        Iterator it = result.iterator();
        List<Integer> userIds = new ArrayList<>();
        while (it.hasNext()) {
            userIds.add(((HashMap<String, Integer>) it.next()).get("reviewerId"));
        }
        return userIds;
    }

    // What documents are left to be approved in review request X?
    @Override
    public List<Integer> getDocsNotApprovedForRequest(int reviewRequest) {
        String query = String.format("MATCH (reviewer:User)<-[:FOR_REVIEWER]-(rr:ReviewRequest {id:5})-[:ON_DOC]->(parent:Doc)-[:HAS_REFERENCE*]->(child:Doc) " +
                "WITH COUNT(distinct reviewer) as approvalsNeeded, child " +
                "OPTIONAL MATCH (child)-[ON_DOC]-(a:Approve)-[:BY_USER]->(reviewer) " +
                "WITH child.id as docId, count(a) as approvals, approvalsNeeded " +
                "WHERE approvals < approvalsNeeded RETURN docId", reviewRequest);
        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        QueryResult result = engine.query(query, Collections.emptyMap());
        Iterator it = result.iterator();
        List<Integer> docIds = new ArrayList<>();
        while (it.hasNext()) {
            docIds.add(((HashMap<String, Integer>) it.next()).get("docId"));
        }
        return docIds;
    }

    // Who approved this document and when?
    @Override
    public List<MockAPI.Approval> getApprovalsForDoc(int docId) {
        return null;
    }

    // What documents in DITA Map Y are left to be approved by User X?
    @Override
    public List<Integer> getDocsNotYetApprovedByUser(int user, int reviewRequest) {
        String query = String.format("MATCH (reviewer:User {id:%d})<-[:FOR_REVIEWER]-(rr:ReviewRequest {id:%d})-[:ON_DOC]->(parent:Doc)-[:HAS_REFERENCE*]->(child:Doc) " +
                "OPTIONAL MATCH child<-[:ON_DOC]-(a:Approve)-[:BY_USER]->reviewer " +
                "WITH child, a WHERE a IS NULL " +
                "RETURN DISTINCT child.id",
                user, reviewRequest);
        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        QueryResult result = engine.query(query, Collections.emptyMap());
        Iterator it = result.iterator();
        List<Integer> docIds = new ArrayList<>();
        while (it.hasNext()) {
            docIds.add(((HashMap<String, Integer>) it.next()).get("child.id"));
        }
        return docIds;

    }

    // Get the history of events. return type TBD... just print out for now.
    @Override
    public void printHistoryForDoc(int doc) {
        String query = String.format("MATCH (u)<-[:BY_USER]-(e:Event)-[:ON_DOC]->(d:Doc) " +
                "return e.ts as ts, labels(e), d.id as Doc, u.id as User order by e.ts DESC");

    }

    @Override
    public int getExplicitDependencies(int doc) {
        String query = String.format("MATCH (parent:Doc {id:%d})-[:HAS_REFERENCE]->(child:Doc) return count(child);", doc);
        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        QueryResult result = engine.query(query, Collections.emptyMap());
        Iterator it = result.iterator();
        return ((HashMap<String, Integer>)it.next()).get("count(child)");



    }

    @Override
    public int getTotalDependencies(int doc) {
        String query = String.format("MATCH (parent:Doc {id:%d})-[:HAS_REFERENCE*]->(child:Doc) return count(child);", doc);
        QueryEngine engine = new RestCypherQueryEngine(restAPI);
        QueryResult result = engine.query(query, Collections.emptyMap());
        Iterator it = result.iterator();
        return ((HashMap<String, Integer>)it.next()).get("count(child)");
    }
}
