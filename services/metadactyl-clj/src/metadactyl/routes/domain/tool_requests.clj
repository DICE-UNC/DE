(ns metadactyl.routes.domain.tool-requests
  (:use [metadactyl.routes.params]
        [ring.swagger.schema :only [describe]]
        [schema.core :only [defschema optional-key enum Any]])
  (:import [java.util UUID]))

(def ToolRequestIdParam (describe UUID "The Tool Requests's UUID"))
(def SubmittedByParam (describe String "The username of the user that submitted the Tool Request"))
(def NameParam (describe String "The name of the tool being installed (should be the file name)"))
(def VersionParam (describe String "The tool's version string"))

(defschema ToolRequestStatus
  {(optional-key :status)
   (describe String
     "The status code of the Tool Request update. The status code is case-sensitive, and if it isn't
      defined in the database already then it will be added to the list of known status codes")

   :status_date
   (describe Long "The timestamp of the Tool Request status update")

   :updated_by
   (describe String "The username of the user that updated the Tool Request status")

   (optional-key :comments)
   (describe String "The administrator comments of the Tool Request status update")})

(defschema ToolRequestStatusUpdate
  (dissoc ToolRequestStatus :updated_by :status_date))

(defschema ToolRequestDetails
  {:id
   ToolRequestIdParam

   :submitted_by
   SubmittedByParam

   (optional-key :phone)
   (describe String "The phone number of the user submitting the request")

   :name
   NameParam

   :description
   (describe String "A brief description of the tool")

   (optional-key :source_url)
   (describe String "A link that can be used to obtain the tool")

   (optional-key :source_upload_file)
   (describe String "The path to a file that has been uploaded into iRODS")

   :documentation_url
   (describe String "A link to the tool documentation")

   :version
   VersionParam

   (optional-key :attribution)
   (describe String "The people or organizations that produced the tool")

   (optional-key :multithreaded)
   (describe Boolean
     "A flag indicating whether or not the tool is multithreaded. This can be `true` to indicate
      that the user requesting the tool knows that it is multithreaded, `false` to indicate that the
      user knows that the tool is not multithreaded, or omitted if the user does not know whether or
      not the tool is multithreaded")

   :test_data_path
   (describe String "The path to a test data file that has been uploaded to iRODS")

   :cmd_line
   (describe String "Instructions for using the tool")

   (optional-key :additional_info)
   (describe String
     "Any additional information that may be helpful during tool installation or validation")

   (optional-key :additional_data_file)
   (describe String
     "Any additional data file that may be helpful during tool installation or validation")

   :architecture
   (describe (enum "32-bit Generic" "64-bit Generic" "Others" "Don't know")
     "One of the architecture names known to the DE. Currently, the valid values are
      `32-bit Generic` for a 32-bit executable that will run in the DE,
      `64-bit Generic` for a 64-bit executable that will run in the DE,
      `Others` for tools run in a virtual machine or interpreter, and
      `Don't know` if the user requesting the tool doesn't know what the architecture is")

   :history
   (describe [ToolRequestStatus] "A history of status updates for this Tool Request")})

(defschema ToolRequest
  (dissoc ToolRequestDetails :id :submitted_by :history))

(defschema ToolRequestSummary
  {:id             ToolRequestIdParam
   :name           NameParam
   :version        VersionParam
   :requested_by   SubmittedByParam
   :date_submitted (describe Long "The timestamp of the Tool Request submission")
   :status         (describe String "The current status of the Tool Request")
   :date_updated   (describe Long "The timestamp of the last Tool Request status update")
   :updated_by     (describe String
                     "The username of the user that last updated the Tool Request status")})

(defschema ToolRequestListing
  {:tool_requests (describe [ToolRequestSummary]
                    "A listing of high level details about tool requests that have been submitted")})

(defschema ToolRequestListingParams
  (merge SecuredPagingParams
    {(optional-key :status)
     (describe String
       "The name of a status code to include in the results. The name of the status code is case
        sensitive. If the status code isn't already defined, it will be added to the database")}))

(defschema StatusCodeListingParams
  (merge SecuredQueryParams
    {(optional-key :filter)
     (describe String
       "If this parameter is set then only the status codes that contain the string passed in this
        query parameter will be listed. This is a case-insensitive search")}))

(defschema StatusCode
  {:id          (describe UUID "The Status Code's UUID")
   :name        (describe String "The Status Code")
   :description (describe String "A brief description of the Status Code")})

(defschema StatusCodeListing
  {:status_codes (describe [StatusCode] "A listing of known Status Codes")})
