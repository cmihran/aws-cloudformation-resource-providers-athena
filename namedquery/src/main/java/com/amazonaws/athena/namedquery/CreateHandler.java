package com.amazonaws.athena.namedquery;

import com.amazonaws.cloudformation.exceptions.CfnGeneralServiceException;
import com.amazonaws.cloudformation.exceptions.CfnInvalidRequestException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.CreateNamedQueryRequest;
import software.amazon.awssdk.services.athena.model.InternalServerException;
import software.amazon.awssdk.services.athena.model.InvalidRequestException;

public class CreateHandler extends BaseHandler<CallbackContext> {
    private AmazonWebServicesClientProxy clientProxy;
    private AthenaClient athenaClient;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        clientProxy = proxy;
        athenaClient = AthenaClient.create();

        return createResource(request.getDesiredResourceState());
    }

    private ProgressEvent<ResourceModel, CallbackContext> createResource(ResourceModel model) {
        model.setNamedQueryId(createNamedQuery(model));
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private String createNamedQuery(final ResourceModel model) {
        final CreateNamedQueryRequest createNamedQueryRequest = CreateNamedQueryRequest.builder()
                .database(model.getDatabase())
                .description(model.getDescription())
                .name(model.getName())
                .queryString(model.getQueryString())
                .build();
        try {
            return clientProxy.injectCredentialsAndInvokeV2(
                    createNamedQueryRequest,
                    athenaClient::createNamedQuery).namedQueryId();
        } catch (InternalServerException e) {
            throw new CfnGeneralServiceException("createNamedQuery", e);
        } catch (InvalidRequestException e) {
            throw new CfnInvalidRequestException(createNamedQueryRequest.toString(), e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}