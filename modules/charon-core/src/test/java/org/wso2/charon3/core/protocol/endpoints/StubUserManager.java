package org.wso2.charon3.core.protocol.endpoints;

import org.wso2.charon3.core.exceptions.*;
import org.wso2.charon3.core.extensions.UserManager;
import org.wso2.charon3.core.objects.Group;
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.utils.codeutils.Node;
import org.wso2.charon3.core.utils.codeutils.SearchRequest;

import java.util.List;
import java.util.Map;

public class StubUserManager implements UserManager {
    @Override
    public User createUser(User user, Map<String, Boolean> requiredAttributes) throws CharonException, ConflictException, BadRequestException {
        return null;
    }

    @Override
    public User getUser(String id, Map<String, Boolean> requiredAttributes) throws CharonException, BadRequestException, NotFoundException {
        return null;
    }

    @Override
    public void deleteUser(String userId) throws NotFoundException, CharonException, NotImplementedException, BadRequestException {

    }

    @Override
    public List<Object> listUsersWithGET(Node node, int startIndex, int count, String sortBy, String sortOrder, Map<String, Boolean> requiredAttributes) throws CharonException, NotImplementedException, BadRequestException {
        return null;
    }

    @Override
    public List<Object> listUsersWithPost(SearchRequest searchRequest, Map<String, Boolean> requiredAttributes) throws CharonException, NotImplementedException, BadRequestException {
        return null;
    }

    @Override
    public User updateUser(User updatedUser, Map<String, Boolean> requiredAttributes) throws NotImplementedException, CharonException, BadRequestException, NotFoundException {
        return null;
    }

    @Override
    public User getMe(String userName, Map<String, Boolean> requiredAttributes) throws CharonException, BadRequestException, NotFoundException {
        return null;
    }

    @Override
    public User createMe(User user, Map<String, Boolean> requiredAttributes) throws CharonException, ConflictException, BadRequestException {
        return null;
    }

    @Override
    public void deleteMe(String userName) throws NotFoundException, CharonException, NotImplementedException, BadRequestException {

    }

    @Override
    public User updateMe(User updatedUser, Map<String, Boolean> requiredAttributes) throws NotImplementedException, CharonException, BadRequestException, NotFoundException {
        return null;
    }

    @Override
    public Group createGroup(Group group, Map<String, Boolean> requiredAttributes) throws CharonException, ConflictException, NotImplementedException, BadRequestException {
        return null;
    }

    @Override
    public Group getGroup(String id, Map<String, Boolean> requiredAttributes) throws NotImplementedException, BadRequestException, CharonException, NotFoundException {
        return null;
    }

    @Override
    public void deleteGroup(String id) throws NotFoundException, CharonException, NotImplementedException, BadRequestException {

    }

    @Override
    public List<Object> listGroupsWithGET(Node node, int startIndex, int count, String sortBy, String sortOrder, Map<String, Boolean> requiredAttributes) throws CharonException, NotImplementedException, BadRequestException {
        return null;
    }

    @Override
    public Group updateGroup(Group oldGroup, Group newGroup, Map<String, Boolean> requiredAttributes) throws NotImplementedException, BadRequestException, CharonException, NotFoundException {
        return null;
    }

    @Override
    public List<Object> listGroupsWithPost(SearchRequest searchRequest, Map<String, Boolean> requiredAttributes) throws NotImplementedException, BadRequestException, CharonException {
        return null;
    }
}
