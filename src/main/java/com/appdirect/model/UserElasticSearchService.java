package com.appdirect.model;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserElasticSearchService {

	@Value("${elasticsearch.index}")
	private String index;

	@Autowired
	private TimeValue timeoutValue;

	@Value("${elasticsearch.collection}")
	private String collection;

	@Autowired
	private Client client;

	@Autowired @Qualifier("elasticsearch")
	private ObjectMapper mapper;
	private String ID_FIELD = "id";;

	public void saveIndex(User user) throws ExecutionException, InterruptedException {
		ListenableActionFuture indexingResult = index(user);
		indexingResult.get();
	}

	public List<Long> searchByBillingId(String billingId){
		QueryBuilder qb = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("billingId", billingId));

		SearchResponse searchIDs = client.prepareSearch(index)
			.setTypes(collection)
			.setExplain(true)
			.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			.setQuery(qb)
			.setExplain(true)
			.addField(ID_FIELD)
			.setTimeout(timeoutValue)
			.execute().actionGet();

		return Arrays.stream(searchIDs.getHits().getHits())
			.map(SearchHit::fields)
			.map(t -> t.get(ID_FIELD))
			.map(SearchHitField::getValue)
			.map(Object::toString)
			.map(p->Long.valueOf(p))
			.collect(Collectors.toList());

	}

	public ListenableActionFuture<IndexResponse> index(final User user) {
		try {
			return new IndexRequestBuilder(client, IndexAction.INSTANCE, index)
				.setType(collection)
				.setTimeout(timeoutValue)
				.setId(user.getId().toString())
				.setContentType(XContentType.JSON)
				.setSource(mapper.writeValueAsString(user))
				.setConsistencyLevel(WriteConsistencyLevel.QUORUM)
				.setOpType(IndexRequest.OpType.INDEX)
				.setRefresh(true)
				.execute();
		} catch (final JsonProcessingException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
