package com.mendeley.api.request;

import android.graphics.Color;

import com.mendeley.api.model.Annotation;
import com.mendeley.api.model.Box;
import com.mendeley.api.model.Discipline;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.DocumentId;
import com.mendeley.api.model.Education;
import com.mendeley.api.model.Employment;
import com.mendeley.api.model.File;
import com.mendeley.api.model.Folder;
import com.mendeley.api.model.Group;
import com.mendeley.api.model.Person;
import com.mendeley.api.model.Photo;
import com.mendeley.api.model.Point;
import com.mendeley.api.model.Profile;
import com.mendeley.api.model.ReadPosition;
import com.mendeley.api.model.UserRole;
import com.mendeley.api.util.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.model.Annotation.PrivacyLevel;

/**
 * This class hold methods to parse json strings to model objects 
 * as well as create json strings from objects that are used by the NetwrokProvider classes.
 */
public class JsonParser {
    private static final String TAG = JsonParser.class.getCanonicalName();

    public static String jsonFromAnnotation(Annotation annotation) throws JSONException {
        JSONObject jAnnotation = new JSONObject();

        jAnnotation.put("id", annotation.id);
        if (annotation.type != null) {
            jAnnotation.put("type", annotation.type.name);
        }
        jAnnotation.put("previous_id", annotation.previousId);
        if (annotation.color != null) {
            jAnnotation.put("color", serializeColor(annotation.color));
        }
        jAnnotation.put("text", annotation.text);
        jAnnotation.put("profile_id", annotation.profileId);

        if (!annotation.positions.isNull()) {
            JSONArray positions = new JSONArray();
            for (int i = 0; i < annotation.positions.size(); i++) {
                Box box = annotation.positions.get(i);
                positions.put(i, serializeBox(box));
            }
            jAnnotation.put("positions", positions);
        }

        jAnnotation.put("created", annotation.created);
        jAnnotation.put("last_modified", annotation.lastModified);
        if (annotation.privacyLevel != null) {
            jAnnotation.put("privacy_level", annotation.privacyLevel.name);
        }
        jAnnotation.put("filehash", annotation.fileHash);
        jAnnotation.put("document_id", annotation.documentId);

        return jAnnotation.toString();
    }

    private static JSONObject serializeColor(int color) throws JSONException {
        JSONObject jColor = new JSONObject();
        jColor.put("r", Color.red(color));
        jColor.put("g", Color.green(color));
        jColor.put("b", Color.blue(color));
        return jColor;
    }

    private static JSONObject serializeBox(Box box) throws JSONException {
        JSONObject topLeft = null;
        JSONObject bottomRight = null;

        if (box.topLeft != null) {
            topLeft = new JSONObject();
            topLeft.put("x", box.topLeft.x);
            topLeft.put("y", box.topLeft.y);
        }
        if (box.bottomRight != null) {
            bottomRight = new JSONObject();
            bottomRight.put("x", box.bottomRight.x);
            bottomRight.put("y", box.bottomRight.y);
        }

        JSONObject bbox = new JSONObject();
        bbox.put("top_left", topLeft);
        bbox.put("bottom_right", bottomRight);
        bbox.put("page", box.page);
        return bbox;
    }

    /**
     * Create a json string from a Document object
     * @param document the Document object
     * @return the json string
     * @throws JSONException
     */
	public static String jsonFromDocument(Document document) throws JSONException {
        final JSONObject jDocument = new JSONObject();

        if (!document.websites.isNull()) {
            JSONArray websites = new JSONArray();
            for (int i = 0; i < document.websites.size(); i++) {
                websites.put(i, document.websites.get(i));
            }
            jDocument.put("websites", websites);
        }

        if (!document.keywords.isNull()) {
            JSONArray keywords = new JSONArray();
            for (int i = 0; i < document.keywords.size(); i++) {
                keywords.put(i, document.keywords.get(i));
            }
            jDocument.put("keywords", keywords);
        }

        if (!document.tags.isNull()) {
            JSONArray tags = new JSONArray();
            for (int i = 0; i < document.tags.size(); i++) {
                tags.put(i, document.tags.get(i));
            }
            jDocument.put("tags", tags);
        }

        if (!document.authors.isNull()) {
            JSONArray authorsJson = jsonFromPersons(document.authors);
            jDocument.put("authors", authorsJson);
        }

        if (!document.editors.isNull()) {
            JSONArray editors = new JSONArray();
            for (int i = 0; i < document.editors.size(); i++) {
                JSONObject editor = new JSONObject();
                editor.put("first_name", document.editors.get(i).firstName);
                editor.put("last_name", document.editors.get(i).lastName);
                editors.put(i, editor);
            }
            jDocument.put("editors", editors);
        }

        if (!document.identifiers.isNull()) {
            JSONObject identifiers = new JSONObject();
            for (String key : document.identifiers.keySet()) {
                identifiers.put(key, document.identifiers.get(key));
            }
            jDocument.put("identifiers", identifiers);
        }

		jDocument.put("title", document.title);
		jDocument.put("type", document.type);
		jDocument.put("id", document.id);
		
		jDocument.put("last_modified", document.lastModified);
		jDocument.put("group_id", document.groupId);
		jDocument.put("profile_id", document.profileId);
		jDocument.put("read", document.read);
		jDocument.put("starred", document.starred);
		jDocument.put("authored", document.authored);
		jDocument.put("confirmed", document.confirmed);
		jDocument.put("hidden", document.hidden);
		jDocument.put("month", document.month);
		jDocument.put("year", document.year);
		jDocument.put("day", document.day);
		jDocument.put("source", document.source);
		jDocument.put("revision", document.revision);
		jDocument.put("abstract", document.abstractString);
		jDocument.put("created", document.created);
		jDocument.put("pages", document.pages);
		jDocument.put("volume", document.volume);
		jDocument.put("issue", document.issue);
		jDocument.put("publisher", document.publisher);
		jDocument.put("city", document.city);
		jDocument.put("edition", document.edition);
		jDocument.put("institution", document.institution);
		jDocument.put("series", document.series);
		jDocument.put("chapter", document.chapter);
        jDocument.put("accessed", document.accessed);
        jDocument.put("file_attached", document.fileAttached);
        jDocument.put("client_data", document.clientData);
        jDocument.put("unique_id", document.uniqueId);

		return jDocument.toString();
	}

    public static JSONArray jsonFromPersons(List<Person> persons) throws JSONException {
        JSONArray authorsJson = new JSONArray();
        for (int i = 0; i < persons.size(); i++) {
            JSONObject author = new JSONObject();
            author.put("first_name", persons.get(i).firstName);
            author.put("last_name", persons.get(i).lastName);
            authorsJson.put(i, author);
        }
        return authorsJson;
    }

    /**
	 * Creating a json string from a Folder object
	 * 
	 * @param folder the Folder object
	 * @return the json string
	 * @throws JSONException
	 */
    public static String jsonFromFolder(Folder folder) throws JSONException {

		JSONObject jFolder = new JSONObject();
		
		jFolder.put("name", folder.name);
		jFolder.put("parent_id", folder.parentId);
		jFolder.put("id", folder.id);
		jFolder.put("group_id", folder.groupId);
		jFolder.put("added", folder.added);
		
		return jFolder.toString();
	}
	
	/**
	 * Creating a json string from a document id string
	 * 
	 * @param documentId the document id string
	 * @return the json string
	 * @throws JSONException
	 */
    public static String jsonFromDocumentId(String documentId) throws JSONException {
		JSONObject jDocument = new JSONObject();		
		jDocument.put("id", documentId);		
		return jDocument.toString();
	}
	
	/**
	 * Creating a list of string document ids from a json string
	 * 
	 * @param jsonString the json string of the document ids
	 * @return the list of string document ids
	 * @throws JSONException
	 */
    public static List<DocumentId> parseDocumentIds(String jsonString) throws JSONException {
		List<DocumentId> documentIds = new ArrayList<DocumentId>();
		JSONArray jsonArray = new JSONArray(jsonString);
		for (int i = 0; i < jsonArray.length(); i++) {	
			documentIds.add(parseDocumentId(jsonArray.get(i).toString()));
		}
		
		return documentIds;
	}

    public static Map<String, String> parseDocumentTypes(String jsonString) throws JSONException{
		Map<String, String> typesMap = new HashMap<String, String>();
		
		JSONArray jsonarray = new JSONArray(jsonString);
		
		for (int i = 0; i < jsonarray.length(); i++) {
			JSONObject jsonObject = jsonarray.getJSONObject(i);
			String key = jsonObject.getString("name");
			String value = jsonObject.getString("description");
			typesMap.put(key, value);
		}
		return typesMap;
	}

	/**
	 * Creating a File object from a json string
	 * 
	 * @param jsonString the json string
	 * @return the File object
	 * @throws JSONException
	 */
    public static File parseFile(String jsonString) throws JSONException {
		File.Builder mendeleyFile = new File.Builder();
		
		JSONObject documentObject = new JSONObject(jsonString);
		
		for (@SuppressWarnings("unchecked") Iterator<String> keysIter = 
				 documentObject.keys(); keysIter.hasNext();) {
		  
			String key = keysIter.next();
            if (key.equals("id")) {
                mendeleyFile.setId(documentObject.getString(key));

            } else if (key.equals("document_id")) {
                mendeleyFile.setDocumentId(documentObject.getString(key));

            } else if (key.equals("mime_type")) {
                mendeleyFile.setMimeType(documentObject.getString(key));

            } else if (key.equals("file_name")) {
                mendeleyFile.setFileName(documentObject.getString(key));

            } else if (key.equals("filehash")) {
                mendeleyFile.setFileHash(documentObject.getString(key));

            } else if (key.equals("size")) {
                mendeleyFile.setFileSize(documentObject.getInt(key));
            }
		}
		
		return mendeleyFile.build();
	}

    public static DocumentId parseDocumentId(String jsonString) throws JSONException {
		DocumentId.Builder mendeleyDocumentId = new DocumentId.Builder();
		
		JSONObject documentObject = new JSONObject(jsonString);
		
		for (@SuppressWarnings("unchecked") Iterator<String> keysIter = 
				 documentObject.keys(); keysIter.hasNext();) {
		  
			String key = keysIter.next();
            if (key.equals("id")) {
                mendeleyDocumentId.setDocumentId(documentObject.getString(key));

            }
		}
		
		return mendeleyDocumentId.build();
	}
	
	/**
	 * Creating a list of Folder objects from a json string
	 * 
	 * @param jsonString the json string
	 * @return the list of Folder objects
	 * @throws JSONException
	 */
    public static List<Folder> parseFolderList(String jsonString) throws JSONException {
		
		List<Folder> folders = new ArrayList<Folder>();
		
		JSONArray jsonarray = new JSONArray(jsonString);
		
		for (int i = 0; i < jsonarray.length(); i++) {
			folders.add(parseFolder(jsonarray.getString(i)));
		}
		
		return folders;
	}
	
	/**
	 * Creating a Folder object from a json string
	 * 
	 * @param jsonString the json string
	 * @return the Folder object
	 * @throws JSONException
	 */
    public static Folder parseFolder(String jsonString) throws JSONException {
        JSONObject folderObject = new JSONObject(jsonString);

        Folder.Builder mendeleyFolder = new Folder.Builder(folderObject.getString("name"));
		
		for (@SuppressWarnings("unchecked") Iterator<String> keysIter =
				folderObject.keys(); keysIter.hasNext();) {
			String key = keysIter.next();
            if (key.equals("parent_id")) {
                mendeleyFolder.setParentId(folderObject.getString(key));

            } else if (key.equals("id")) {
                mendeleyFolder.setId(folderObject.getString(key));

            } else if (key.equals("group_id")) {
                mendeleyFolder.setGroupId(folderObject.getString(key));

            } else if (key.equals("added")) {
                mendeleyFolder.setAdded(folderObject.getString(key));

            }
		}
		
		return mendeleyFolder.build();
	}

    /**
     * Creating a UserRole object from a json string
     *
     * @param jsonString the json string
     * @return the UserRole object
     * @throws JSONException
     */
    public static UserRole parseUserRole(String jsonString) throws JSONException {
        JSONObject userRoleObject = new JSONObject(jsonString);

        UserRole.Builder mendeleyUserRole = new UserRole.Builder();

        for (@SuppressWarnings("unchecked") Iterator<String> keysIter =
                     userRoleObject.keys(); keysIter.hasNext();) {
            String key = keysIter.next();
            if (key.equals("profile_id")) {
                mendeleyUserRole.setProfileId(userRoleObject.getString(key));

            } else if (key.equals("joined")) {
                mendeleyUserRole.setJoined(userRoleObject.getString(key));

            } else if (key.equals("role")) {
                mendeleyUserRole.setRole(userRoleObject.getString(key));

            }
        }

        return mendeleyUserRole.build();
    }

    public static Annotation parseAnnotation(String jsonString) throws JSONException {
        Annotation.Builder builder = new Annotation.Builder();

        JSONObject jAnnotation = new JSONObject(jsonString);

        for (Iterator<String> keysIter = jAnnotation.keys(); keysIter.hasNext(); ) {
            String key = keysIter.next();
            if (key.equals("id")) {
                builder.setId(jAnnotation.getString(key));

            } else if (key.equals("type")) {
                builder.setType(Annotation.Type.fromName(jAnnotation.getString(key)));

            } else if (key.equals("previous_id")) {
                builder.setPreviousId(jAnnotation.getString(key));

            } else if (key.equals("color")) {
                builder.setColor(parseColor(jAnnotation.getJSONObject(key)));

            } else if (key.equals("text")) {
                builder.setText(jAnnotation.getString(key));

            } else if (key.equals("profile_id")) {
                builder.setProfileId(jAnnotation.getString(key));

            } else if (key.equals("positions")) {
                JSONArray jPositions = jAnnotation.getJSONArray(key);
                builder.setPositions(parseBoundingBoxes(jPositions));

            } else if (key.equals("created")) {
                builder.setCreated(jAnnotation.getString(key));

            } else if (key.equals("last_modified")) {
                builder.setLastModified(jAnnotation.getString(key));

            } else if (key.equals("privacy_level")) {
                builder.setPrivacyLevel(PrivacyLevel.fromName(jAnnotation.getString(key)));

            } else if (key.equals("filehash")) {
                builder.setFileHash(jAnnotation.getString(key));

            } else if (key.equals("document_id")) {
                builder.setDocumentId(jAnnotation.getString(key));

            }
        }
        return builder.build();
    }

    private static int parseColor(JSONObject jColor) throws JSONException {
        int r = jColor.getInt("r");
        int g = jColor.getInt("g");
        int b = jColor.getInt("b");
        return Color.rgb(r, g, b);
    }

    private static List<Box> parseBoundingBoxes(JSONArray jPositions) throws JSONException {
        List<Box> boxes = new ArrayList<Box>();
        for (int i = 0; i < jPositions.length(); i++) {
            JSONObject jBox = jPositions.getJSONObject(i);

            Point topLeft = null;
            Point bottomRight = null;
            Integer page = null;

            if (jBox.has("page")) {
                page = jBox.getInt("page");
            }
            if (jBox.has("top_left")) {
                JSONObject jTopLeft = jBox.getJSONObject("top_left");
                topLeft = new Point(jTopLeft.getDouble("x"), jTopLeft.getDouble("y"));
            }
            if (jBox.has("bottom_right")) {
                JSONObject jBottomRight = jBox.getJSONObject("bottom_right");
                bottomRight = new Point(jBottomRight.getDouble("x"), jBottomRight.getDouble("y"));
            }

            Box box = new Box(topLeft, bottomRight, page);
            boxes.add(box);
        }
        return boxes;
    }

    /**
	 * Creating a Profile object from a json string
	 * 
	 * @param jsonString the json string
	 * @return the Profile object
	 * @throws JSONException
	 */
    public static Profile parseProfile(String jsonString) throws JSONException {
		Profile.Builder mendeleyProfile = new Profile.Builder();
		
		JSONObject profileObject = new JSONObject(jsonString);
		 
		for (@SuppressWarnings("unchecked") Iterator<String> keysIter = 
				profileObject.keys(); keysIter.hasNext();) {
		  
			String key = keysIter.next();
            if (key.equals("location")) {
                mendeleyProfile.setLocation(profileObject.getString(key));

            } else if (key.equals("id")) {
                mendeleyProfile.setId(profileObject.getString(key));

            } else if (key.equals("display_name")) {
                mendeleyProfile.setDisplayName(profileObject.getString(key));

            } else if (key.equals("user_type")) {
                mendeleyProfile.setUserType(profileObject.getString(key));

            } else if (key.equals("url")) {
                mendeleyProfile.setUrl(profileObject.getString(key));

            } else if (key.equals("email")) {
                mendeleyProfile.setEmail(profileObject.getString(key));

            } else if (key.equals("link")) {
                mendeleyProfile.setLink(profileObject.getString(key));

            } else if (key.equals("first_name")) {
                mendeleyProfile.setFirstName(profileObject.getString(key));

            } else if (key.equals("last_name")) {
                mendeleyProfile.setLastName(profileObject.getString(key));

            } else if (key.equals("research_interests")) {
                mendeleyProfile.setResearchInterests(profileObject.getString(key));

            } else if (key.equals("academic_status")) {
                mendeleyProfile.setAcademicStatus(profileObject.getString(key));

            } else if (key.equals("verified")) {
                mendeleyProfile.setVerified(profileObject.getBoolean(key));

            } else if (key.equals("created_at")) {
                mendeleyProfile.setCreatedAt(profileObject.getString(key));

            } else if (key.equals("discipline")) {
                JSONObject disciplineObject = profileObject.getJSONObject(key);
                Discipline discipline = new Discipline();
                if (disciplineObject.has("name")) {
                    discipline.name = disciplineObject.getString("name");
                }
                mendeleyProfile.setDiscipline(discipline);

            } else if (key.equals("photo")) {
                JSONObject photoObject = profileObject.getJSONObject(key);
                Photo photo = parsePhoto(photoObject);

                mendeleyProfile.setPhoto(photo);

            } else if (key.equals("education")) {
                JSONArray educationArray = profileObject.getJSONArray(key);
                ArrayList<Education> educationList = new ArrayList<Education>();

                for (int i = 0; i < educationArray.length(); i++) {

                    JSONObject educationObject = educationArray.getJSONObject(i);
                    educationList.add(parseEducation(educationObject));

                }

                mendeleyProfile.setEducation(educationList);

            } else if (key.equals("employment")) {
                JSONArray employmentArray = profileObject.getJSONArray(key);
                ArrayList<Employment> employmentList = new ArrayList<Employment>();

                for (int i = 0; i < employmentArray.length(); i++) {
                    JSONObject employmentObject = employmentArray.getJSONObject(i);
                    employmentList.add(parseEmployment(employmentObject));
                }

                mendeleyProfile.setEmployment(employmentList);

            }
		}
		
		return mendeleyProfile.build();
	}

    public static Photo parsePhoto(JSONObject photoObject) {
        return new Photo(
                                photoObject.optString("original"),
                                photoObject.optString("standard"),
                                photoObject.optString("square"));
    }

    private static Employment parseEmployment(JSONObject employmentObject) throws JSONException {
        Employment.Builder employmentBuilder = new Employment.Builder();

        for (@SuppressWarnings("unchecked") Iterator<String> employmentIter =
                     employmentObject.keys(); employmentIter.hasNext(); ) {

            String employmentKey = employmentIter.next();

            if (employmentKey.equals("id")) {
                employmentBuilder.setId(employmentObject.getString(employmentKey));

            } else if (employmentKey.equals("institution")) {
                employmentBuilder.setInstitution(employmentObject.getString(employmentKey));

            } else if (employmentKey.equals("position")) {
                employmentBuilder.setPosition(employmentObject.getString(employmentKey));

            } else if (employmentKey.equals("start_date")) {
                employmentBuilder.setStartDate(employmentObject.getString(employmentKey));

            } else if (employmentKey.equals("end_date")) {
                employmentBuilder.setEndDate(employmentObject.getString(employmentKey));

            } else if (employmentKey.equals("website")) {
                employmentBuilder.setWebsite(employmentObject.getString(employmentKey));

            } else if (employmentKey.equals("classes")) {
                JSONArray classesJsonArray = employmentObject.getJSONArray(employmentKey);
                List<String> classesArray = new ArrayList<String>();
                for (int i = 0; i < classesJsonArray.length(); i++) {
                    classesArray.add(classesJsonArray.getString(i));
                }
                employmentBuilder.setClasses(classesArray);

            } else if (employmentKey.equals("is_main_employment")) {
                employmentBuilder.setIsMainEmployment(employmentObject.getBoolean(employmentKey));

            }
        }
        return employmentBuilder.build();
    }

    private static Education parseEducation(JSONObject educationObject) throws JSONException {
        Education.Builder education = new Education.Builder();

        for (@SuppressWarnings("unchecked") Iterator<String> educationIter =
                     educationObject.keys(); educationIter.hasNext(); ) {

            String educationKey = educationIter.next();
            if (educationKey.equals("id")) {
                education.setId(educationObject.getString(educationKey));

            } else if (educationKey.equals("degree")) {
                education.setDegree(educationObject.getString(educationKey));

            } else if (educationKey.equals("institution")) {
                education.setInstitution(educationObject.getString(educationKey));

            } else if (educationKey.equals("start_date")) {
                education.setStartDate(educationObject.getString(educationKey));

            } else if (educationKey.equals("end_date")) {
                education.setEndDate(educationObject.getString(educationKey));

            } else if (educationKey.equals("website")) {
                education.setWebsite(educationObject.getString(educationKey));

            }
        }
        return education.build();
    }

    /**
     * Creating a Group object from a json string
     *
     * @param jsonString the json string
     * @return the Group object
     * @throws JSONException
     */
    public static Group parseGroup(String jsonString) throws JSONException {
        JSONObject groupObject = new JSONObject(jsonString);

        Group.Builder mendeleyGroup = new Group.Builder();

        for (@SuppressWarnings("unchecked")
             Iterator<String> keysIter = groupObject.keys(); keysIter.hasNext(); ) {

            String key = keysIter.next();
            if (key.equals("id")) {
                mendeleyGroup.setId(groupObject.getString(key));

            } else if (key.equals("created")) {
                mendeleyGroup.setCreated(groupObject.getString(key));

            } else if (key.equals("owning_profile_id")) {
                mendeleyGroup.setOwningProfileId(groupObject.getString(key));

            } else if (key.equals("link")) {
                mendeleyGroup.setLink(groupObject.getString(key));

            } else if (key.equals("role")) {
                mendeleyGroup.setRole(Group.Role.fromValue(groupObject.getString(key)));

            } else if (key.equals("access_level")) {
                mendeleyGroup.setAccessLevel(Group.AccessLevel.fromValue(groupObject.getString(key)));

            } else if (key.equals("name")) {
                mendeleyGroup.setName(groupObject.getString(key));

            } else if (key.equals("description")) {
                mendeleyGroup.setDescription(groupObject.getString(key));

            } else if (key.equals("tags")) {
                JSONArray tagsJsonArray = groupObject.getJSONArray(key);
                ArrayList<String> tagsArray = new ArrayList<String>();
                for (int i = 0; i < tagsJsonArray.length(); i++) {
                    tagsArray.add(tagsJsonArray.getString(i));
                }
                mendeleyGroup.setTags(tagsArray);

            } else if (key.equals("webpage")) {
                mendeleyGroup.setWebpage(groupObject.getString(key));

            } else if (key.equals("disciplines")) {
                JSONArray disciplinesJsonArray = groupObject.getJSONArray(key);
                ArrayList<String> disciplinesArray = new ArrayList<String>();
                for (int i = 0; i < disciplinesJsonArray.length(); i++) {
                    disciplinesArray.add(disciplinesJsonArray.getString(i));
                }
                mendeleyGroup.setDisciplines(disciplinesArray);

            } else if (key.equals("photo")) {
                JSONObject photoObject = groupObject.getJSONObject(key);
                Photo photo = parsePhoto(photoObject);
                mendeleyGroup.setPhoto(photo);

            }
        }

        return mendeleyGroup.build();
    }

	/**
	 * Creating a Document object from a json string
	 * 
	 * @param jsonString the json string
	 * @return the Document object
	 * @throws JSONException
	 */
    public static Document parseDocument(String jsonString) throws JSONException {
        JSONObject documentObject = new JSONObject(jsonString);

		Document.Builder bld = new Document.Builder();

		for (@SuppressWarnings("unchecked")
             Iterator<String> keysIter = documentObject.keys(); keysIter.hasNext(); ) {

			String key = keysIter.next();
            if (key.equals("title")) {
                bld.setTitle(documentObject.getString(key));

            } else if (key.equals("type")) {
                bld.setType(documentObject.getString(key));

            } else if (key.equals("last_modified")) {
                bld.setLastModified(documentObject.getString(key));

            } else if (key.equals("group_id")) {
                bld.setGroupId(documentObject.getString(key));

            } else if (key.equals("profile_id")) {
                bld.setProfileId(documentObject.getString(key));

            } else if (key.equals("read")) {
                bld.setRead(documentObject.getBoolean(key));

            } else if (key.equals("starred")) {
                bld.setStarred(documentObject.getBoolean(key));

            } else if (key.equals("authored")) {
                bld.setAuthored(documentObject.getBoolean(key));

            } else if (key.equals("confirmed")) {
                bld.setConfirmed(documentObject.getBoolean(key));

            } else if (key.equals("hidden")) {
                bld.setHidden(documentObject.getBoolean(key));

            } else if (key.equals("id")) {
                bld.setId(documentObject.getString(key));

            } else if (key.equals("month")) {
                bld.setMonth(documentObject.getInt(key));

            } else if (key.equals("year")) {
                bld.setYear(documentObject.getInt(key));

            } else if (key.equals("day")) {
                bld.setDay(documentObject.getInt(key));

            } else if (key.equals("source")) {
                bld.setSource(documentObject.getString(key));

            } else if (key.equals("revision")) {
                bld.setRevision(documentObject.getString(key));

            } else if (key.equals("created")) {
                bld.setCreated(documentObject.getString(key));

            } else if (key.equals("abstract")) {
                bld.setAbstractString(documentObject.getString(key));

            } else if (key.equals("pages")) {
                bld.setPages(documentObject.getString(key));

            } else if (key.equals("volume")) {
                bld.setVolume(documentObject.getString(key));

            } else if (key.equals("issue")) {
                bld.setIssue(documentObject.getString(key));

            } else if (key.equals("publisher")) {
                bld.setPublisher(documentObject.getString(key));

            } else if (key.equals("city")) {
                bld.setCity(documentObject.getString(key));

            } else if (key.equals("edition")) {
                bld.setEdition(documentObject.getString(key));

            } else if (key.equals("institution")) {
                bld.setInstitution(documentObject.getString(key));

            } else if (key.equals("series")) {
                bld.setSeries(documentObject.getString(key));

            } else if (key.equals("chapter")) {
                bld.setChapter(documentObject.getString(key));

            } else if (key.equals("client_data")) {
                bld.setClientData(documentObject.getString(key));

            } else if (key.equals("unique_id")) {
                bld.setUniqueId(documentObject.getString(key));

            } else if (key.equals("authors")) {
                final JSONArray authors = documentObject.getJSONArray(key);
                final ArrayList<Person> authorsList = parsePersons(authors);

                bld.setAuthors(authorsList);

            } else if (key.equals("editors")) {
                JSONArray editors = documentObject.getJSONArray(key);
                ArrayList<Person> editorsList = new ArrayList<Person>();

                for (int i = 0; i < editors.length(); i++) {
                    Person editor = new Person(
                            editors.getJSONObject(i).optString("first_name"),
                            editors.getJSONObject(i).getString("last_name"));
                    editorsList.add(editor);
                }

                bld.setEditors(editorsList);

            } else if (key.equals("identifiers")) {
                JSONObject identifiersObject = documentObject.getJSONObject(key);
                HashMap<String, String> identifiersMap = new HashMap<String, String>();

                for (@SuppressWarnings("unchecked") Iterator<String> identifierIter =
                             identifiersObject.keys(); identifierIter.hasNext(); ) {

                    String identifierKey = identifierIter.next();
                    identifiersMap.put(identifierKey, identifiersObject.getString(identifierKey));
                }

                bld.setIdentifiers(identifiersMap);

            } else if (key.equals("tags")) {
                JSONArray tags = documentObject.getJSONArray(key);
                ArrayList<String> tagsList = new ArrayList<String>();

                for (int i = 0; i < tags.length(); i++) {
                    tagsList.add(tags.getString(i));
                }

                bld.setTags(tagsList);

            } else if (key.equals("accessed")) {
                bld.setAccessed(documentObject.getString(key));

            } else if (key.equals("file_attached")) {
                bld.setFileAttached(documentObject.getBoolean(key));

            } else if (key.equals("keywords")) {
                JSONArray keywords = documentObject.getJSONArray(key);
                ArrayList<String> keywordsList = new ArrayList<String>();

                for (int i = 0; i < keywords.length(); i++) {
                    keywordsList.add(keywords.getString(i));
                }

                bld.setKeywords(keywordsList);

            } else if (key.equals("websites")) {
                JSONArray websites = documentObject.getJSONArray(key);
                ArrayList<String> websitesList = new ArrayList<String>();

                for (int i = 0; i < websites.length(); i++) {
                    websitesList.add(websites.getString(i));
                }
                bld.setWebsites(websitesList);

            }
		}

		return bld.build();
	}

    public static ArrayList<Person> parsePersons(JSONArray authors) throws JSONException {
        final ArrayList<Person> authorsList = new ArrayList<Person>();

        for (int i = 0; i < authors.length(); i++) {
            final Person author = new Person(
                    authors.getJSONObject(i).optString("first_name"),
                    authors.getJSONObject(i).getString("last_name"));
            authorsList.add(author);
        }
        return authorsList;
    }

    /**
	 * Creating a list of File objects from a json string
	 * 
	 * @param jsonString the json string
	 * @return the list of File objects
	 * @throws JSONException
	 */
    public static List<File> parseFileList(String jsonString) throws JSONException {
		
		List<File> files = new ArrayList<File>();
		
		JSONArray jsonarray = new JSONArray(jsonString);
		
		for (int i = 0; i < jsonarray.length(); i++) {
			files.add(parseFile(jsonarray.getString(i)));
		}
		
		return files;
	}
	
	/**
	 *  Creating a list of Document objects from a json string
	 * 
	 * @param jsonString the json string
	 * @return the list of Document objects
	 * @throws JSONException
	 */
    public static List<Document> parseDocumentList(String jsonString) throws JSONException {

        List<Document> documents = new ArrayList<Document>();

        JSONArray jsonarray = new JSONArray(jsonString);

        for (int i = 0; i < jsonarray.length(); i++) {
            documents.add(parseDocument(jsonarray.getString(i)));
        }

        return documents;
    }

    public static List<Annotation> parseAnnotationList(String jsonString) throws JSONException {
        List<Annotation> annotations = new ArrayList<Annotation>();
        JSONArray jsonarray = new JSONArray(jsonString);

        for (int i = 0; i < jsonarray.length(); i++) {
            annotations.add(parseAnnotation(jsonarray.getString(i)));
        }
        return annotations;
    }

    /**
     *  Creating a list of UserRole objects from a json string
     *
     * @param jsonString the json string
     * @return the list of UserRole objects
     * @throws JSONException
     */
    public static List<UserRole> parseUserRoleList(String jsonString) throws JSONException {

        List<UserRole> userRoles = new ArrayList<UserRole>();

        JSONArray jsonarray = new JSONArray(jsonString);

        for (int i = 0; i < jsonarray.length(); i++) {
            userRoles.add(parseUserRole(jsonarray.getString(i)));
        }

        return userRoles;
    }

    /**
     *  Creating a list of Group objects from a json string
     *
     * @param jsonString the json string
     * @return the list of Group objects
     * @throws JSONException
     */
    public static List<Group> parseGroupList(String jsonString) throws JSONException {

        List<Group> groups = new ArrayList<Group>();

        JSONArray jsonarray = new JSONArray(jsonString);

        for (int i = 0; i < jsonarray.length(); i++) {
            groups.add(parseGroup(jsonarray.getString(i)));
        }

        return groups;
    }

    /**
     * Creating a list of {@link ReadPosition} from a JSON string
     *
     * @param jsonString the json string
     * @return the list of objects
     * @throws JSONException
     */
    public static List<ReadPosition> parseReadPositionList(String jsonString) throws JSONException, ParseException {

        List<ReadPosition> readPositions = new LinkedList<>();

        JSONArray jsonarray = new JSONArray(jsonString);

        for (int i = 0; i < jsonarray.length(); i++) {
            readPositions.add(parseReadPosition(jsonarray.getString(i)));
        }

        return readPositions;
    }

    /**
     * Creating a {@link ReadPosition} from a JSON string
     *
     * @param jsonString the json string
     * @return the list of objects
     * @throws JSONException
     */
    public static ReadPosition parseReadPosition(String jsonString) throws JSONException, ParseException {
        JSONObject jsonObject = new JSONObject(jsonString);

        ReadPosition.Builder bld = new ReadPosition.Builder();

        for (@SuppressWarnings("unchecked") Iterator<String> keysIter = jsonObject.keys(); keysIter.hasNext();) {
            String key = keysIter.next();

            if (key.equals("id")) {
                bld.setId(jsonObject.getString(key));

            } else if (key.equals("file_id")) {
                bld.setFileId(jsonObject.getString(key));

            } else if (key.equals("page")) {
                bld.setPage(jsonObject.getInt(key));

            } else if (key.equals("vertical_position")) {
                bld.setVerticalPosition(jsonObject.getInt(key));

            } else if (key.equals("date")) {
                bld.setDate(DateUtils.parseMendeleyApiTimestamp(jsonObject.getString(key)));
            }
        }

        return bld.build();
    }

    /**
     * Creating a String from a {@link ReadPosition}
     */
    public static String jsonFromReadPosition(ReadPosition readPosition) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", readPosition.id);
        jsonObject.put("file_id", readPosition.fileId);
        jsonObject.put("page", readPosition.page);
        jsonObject.put("vertical_position", readPosition.verticalPosition);
        jsonObject.put("date", DateUtils.formatMendeleyApiTimestamp(readPosition.date));

        return jsonObject.toString();
    }

    public static List<String> parseApplicationFeatures(String jsonString) throws JSONException {
        List<String> featureList = new LinkedList<String>();

        JSONArray jsonarray = new JSONArray(jsonString);

        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonObject = jsonarray.getJSONObject(i);
            featureList.add(jsonObject.getString("name"));
        }
        return featureList;
    }
}
