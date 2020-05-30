#!/bin/bash
# redo changes in places that are not protected agains code generation

cd "${BASH_SOURCE%/*}"

# my POST handler throws and exception
sed -i "s|public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String serviceProviderId)|public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String serviceProviderId) throws OslcResourceException|" \
../compilation/src/main/java/verifit/compilation/VeriFitCompilationManager.java
sed -i "s|public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String serviceProviderId)|public static AutomationRequest createAutomationRequest(HttpServletRequest httpServletRequest, final AutomationRequest aResource, final String serviceProviderId) throws OslcResourceException|" \
../analysis/src/main/java/verifit/analysis/VeriFitAnalysisManager.java

# catch the added exception and send an error response
sed -i "s|return Response.created(newResource.getAbout()).entity(newResource).header(VeriFitCompilationConstants.HDR_OSLC_VERSION, VeriFitCompilationConstants.OSLC_VERSION_V2).build();|return Response.created(newResource.getAbout()).entity(newResource).header(VeriFitCompilationConstants.HDR_OSLC_VERSION, VeriFitCompilationConstants.OSLC_VERSION_V2).build();\\n        } catch (OslcResourceException e) {\\n               Error errorResource = new Error();\\n               errorResource.setStatusCode(\"400\");\\n               errorResource.setMessage(e.getMessage());\\n               return Response.status(400).entity(errorResource).build();|" \
../compilation/src/main/java/verifit/compilation/services/ServiceProviderService1.java
sed -i "s|return Response.created(newResource.getAbout()).entity(newResource).header(VeriFitAnalysisConstants.HDR_OSLC_VERSION, VeriFitAnalysisConstants.OSLC_VERSION_V2).build();|return Response.created(newResource.getAbout()).entity(newResource).header(VeriFitAnalysisConstants.HDR_OSLC_VERSION, VeriFitAnalysisConstants.OSLC_VERSION_V2).build();\\n        } catch (OslcResourceException e) {\\n               Error errorResource = new Error();\\n               errorResource.setStatusCode(\"400\");\\n               errorResource.setMessage(e.getMessage());\\n               return Response.status(400).entity(errorResource).build();|" \
../analysis/src/main/java/verifit/analysis/services/ServiceProviderService1.java
