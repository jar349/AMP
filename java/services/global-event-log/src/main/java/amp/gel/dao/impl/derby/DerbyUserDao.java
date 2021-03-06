package amp.gel.dao.impl.derby;

import static amp.gel.domain.ColumnHeader.COUNT;
import static amp.gel.domain.ColumnHeader.USER;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import amp.gel.dao.UserDao;
import amp.gel.domain.Row;
import amp.gel.domain.Table;

public class DerbyUserDao implements UserDao {

	private static final Logger logger = LoggerFactory
			.getLogger(DerbyUserDao.class);

	private static final String UNIQUE_USERS_QUERY = "SELECT DISTINCT senderidentity FROM envelope WHERE creationtime BETWEEN :start AND :stop";

	private static final String COUNT_FOR_USER_QUERY = "SELECT COUNT(senderidentity) FROM envelope WHERE senderidentity = :user AND creationtime BETWEEN :start AND :stop";

	private static final String TYPE_CONDITIONAL = " AND type = :type";

	private EntityManager entityManager;

	@PersistenceContext
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * amp.gel.dao.impl.derby.UserDao#getEventsByUser(org.joda.time.DateTime,
	 * org.joda.time.DateTime)
	 */
	@Transactional(readOnly = true)
	public Table getEventsByUser(DateTime start, DateTime stop)
			throws Exception {
		Table table = getEventsByUserForType(start, stop, null);
		return table;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * amp.gel.dao.impl.derby.UserDao#getEventsByUserForType(org.joda.time.DateTime
	 * , org.joda.time.DateTime, java.lang.String)
	 */
	@Transactional(readOnly = true)
	public Table getEventsByUserForType(DateTime start, DateTime stop,
			String type) throws Exception {
		Table table = new Table();
		table.setColumnHeaders(Arrays.asList(USER, COUNT));

		List<String> users = getUsers(start, stop, type);
		for (String user : users) {
			Object result = getCountForUser(start, stop, user, type);
			Object[] objects = Arrays.asList(user, result).toArray();

			Row row = new Row(objects);
			table.addRow(row);
		}

		logger.debug(table.toString());
		return table;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amp.gel.dao.impl.derby.UserDao#getUsers(org.joda.time.DateTime,
	 * org.joda.time.DateTime)
	 */
	@Transactional(readOnly = true)
	public List<String> getUsers(DateTime start, DateTime stop) {
		List<String> users = getUsers(start, stop, null);
		logger.debug(users.toString());
		return users;
	}

	@SuppressWarnings("unchecked")
	private List<String> getUsers(DateTime start, DateTime stop, String type) {
		String sql = UNIQUE_USERS_QUERY;
		if (StringUtils.isNotBlank(type)) {
			sql += TYPE_CONDITIONAL;
		}

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("start", start.toDate());
		query.setParameter("stop", stop.toDate());

		if (StringUtils.isNotBlank(type)) {
			query.setParameter("type", type);
		}

		List<String> results = (List<String>) query.getResultList();
		return results;
	}

	private Object getCountForUser(DateTime start, DateTime stop, String user,
			String type) {
		String sql = COUNT_FOR_USER_QUERY;
		if (StringUtils.isNotBlank(type)) {
			sql += TYPE_CONDITIONAL;
		}

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("user", user);
		query.setParameter("start", start.toDate());
		query.setParameter("stop", stop.toDate());

		if (StringUtils.isNotBlank(type)) {
			query.setParameter("type", type);
		}

		Object result = query.getSingleResult();
		return result;
	}

}
