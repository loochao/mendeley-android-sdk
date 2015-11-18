package com.mendeley.api.request;

import android.graphics.Color;
import android.util.JsonReader;

import com.mendeley.api.model.Annotation;
import com.mendeley.api.model.Discipline;
import com.mendeley.api.model.Document;
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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.model.Annotation.PrivacyLevel;

/**
 * This class hold methods to parse json strings to model objects 
 * as well as create json strings from objects that are used by the NetwrokProvider classes.
 */
public class JsonParser {

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
                Annotation.Position position = annotation.positions.get(i);
                positions.put(i, serializePosition(position));
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

    private static JSONObject serializePosition(Annotation.Position position) throws JSONException {
        JSONObject topLeft = null;
        JSONObject bottomRight = null;

        if (position.topLeft != null) {
            topLeft = new JSONObject();
            topLeft.put("x", position.topLeft.x);
            topLeft.put("y", position.topLeft.y);
        }
        if (position.bottomRight != null) {
            bottomRight = new JSONObject();
            bottomRight.put("x", position.bottomRight.x);
            bottomRight.put("y", position.bottomRight.y);
        }

        JSONObject bbox = new JSONObject();
        bbox.put("top_left", topLeft);
        bbox.put("bottom_right", bottomRight);
        bbox.put("page", position.page);
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
	 * @param reader
	 * @return the list of string document ids
	 * @throws JSONException
	 */
    public static List<String> parseDocumentIds(JsonReader reader) throws JSONException, IOException {
		final List<String> documentIds = new ArrayList<String>();

        reader.beginArray();
		while (reader.hasNext()) {
			documentIds.add(parseDocumentId(reader));
		}
        reader.endArray();
		
		return documentIds;
	}

    public static Map<String, String> parseStringsMap(JsonReader reader) throws JSONException, IOException {
		final Map<String, String> typesMap = new HashMap<String, String>();

        reader.beginArray();

		while (reader.hasNext()) {
            parseDocumentTypeEntry(reader, typesMap);
		}

        reader.endArray();
		return typesMap;
	}

    private static void parseDocumentTypeEntry(JsonReader reader, Map<String, String> map) throws IOException {
        reader.beginObject();

        String nameValue = null;
        String descriptionValue = null;

        while (reader.hasNext()) {
            final String key = reader.nextName();
            if (key.equals("name")) {
                nameValue = reader.nextString();
            } else if (key.equals("description")) {
                descriptionValue = reader.nextString();
            } else {
                reader.skipValue();
            }
        }

        map.put(nameValue, descriptionValue);

        reader.endObject();
    }

    /**
	 * Creating a File object from a json string
	 * 
	 * @param reader
	 * @return the File object
	 * @throws JSONException
	 */
    public static File parseFile(JsonReader reader) throws JSONException, IOException {
        reader.beginObject();

        final File.Builder builder = new File.Builder();

		while (reader.hasNext()) {
		  
			String key = reader.nextName();
            if (key.equals("id")) {
                builder.setId(reader.nextString());

            } else if (key.equals("document_id")) {
                builder.setDocumentId(reader.nextString());

            } else if (key.equals("mime_type")) {
                builder.setMimeType(reader.nextString());

            } else if (key.equals("file_name")) {
                builder.setFileName(reader.nextString());

            } else if (key.equals("filehash")) {
                builder.setFileHash(reader.nextString());

            } else if (key.equals("size")) {
                builder.setFileSize(reader.nextInt());
            } else {
                reader.skipValue();
            }
		}
        reader.endObject();
		
		return builder.build();
	}

    public static String parseDocumentId(JsonReader reader) throws JSONException, IOException {
        String id = null;
		
		reader.beginObject();

        while (reader.hasNext()) {
		  
			final String key = reader.nextName();
            if (key.equals("id")) {
                id = reader.nextString();
            } else {
                reader.skipValue();
            }
		}

        reader.endObject();
		
		return id;
	}
	
	/**
	 * Creating a list of Folder objects from a json string
	 * 
	 * @param reader
	 * @return the list of Folder objects
	 * @throws JSONException
	 */
    public static List<Folder> parseFolderList(JsonReader reader) throws JSONException, IOException {
		
		final List<Folder> folders = new ArrayList<Folder>();
		
		reader.beginArray();
		while (reader.hasNext()) {
            folders.add(parseFolder(reader));
		}
		reader.endArray();

		return folders;
	}
	
	/**
	 * Creating a Folder object from a json string
	 * 
	 * @param reader
	 * @return the Folder object
	 * @throws JSONException
	 */
    public static Folder parseFolder(JsonReader reader) throws JSONException, IOException {
        final Folder.Builder bld = new Folder.Builder();

        reader.beginObject();
		while (reader.hasNext()) {
            final String key = reader.nextName();

            if (key.equals("name")) {
                bld.setName(reader.nextString());
            } else if (key.equals("parent_id")) {
                bld.setParentId(reader.nextString());
            } else if (key.equals("id")) {
                bld.setId(reader.nextString());
            } else if (key.equals("group_id")) {
                bld.setGroupId(reader.nextString());
            } else if (key.equals("added")) {
                bld.setAdded(reader.nextString());
            } else {
                reader.skipValue();
            }
		}
        reader.endObject();
		
		return bld.build();
	}

    /**
     * Creating a UserRole object from a json string
     *
     * @param reader
     * @return the UserRole object
     * @throws JSONException
     */
    public static UserRole parseUserRole(JsonReader reader) throws JSONException, IOException {
        final UserRole.Builder mendeleyUserRole = new UserRole.Builder();
        reader.beginObject();

        while (reader.hasNext()) {
            final String key = reader.nextName();

            if (key.equals("profile_id")) {
                mendeleyUserRole.setProfileId(reader.nextString());
            } else if (key.equals("joined")) {
                mendeleyUserRole.setJoined(reader.nextString());
            } else if (key.equals("role")) {
                mendeleyUserRole.setRole(reader.nextString());
            } else {
                reader.skipValue();
            }

        }

        reader.endObject();
        return mendeleyUserRole.build();
    }

    public static Annotation parseAnnotation(JsonReader reader) throws JSONException, IOException {
        final Annotation.Builder builder = new Annotation.Builder();

        reader.beginObject();

        while (reader.hasNext()) {
            final String key = reader.nextName();

            if (key.equals("id")) {
                builder.setId(reader.nextString());

            } else if (key.equals("type")) {
                builder.setType(Annotation.Type.fromName(reader.nextString()));

            } else if (key.equals("previous_id")) {
                builder.setPreviousId(reader.nextString());

            } else if (key.equals("color")) {
                builder.setColor(parseColor(reader));

            } else if (key.equals("text")) {
                builder.setText(reader.nextString());

            } else if (key.equals("profile_id")) {
                builder.setProfileId(reader.nextString());

            } else if (key.equals("positions")) {
                builder.setPositions(parseBoundingPositions(reader));

            } else if (key.equals("created")) {
                builder.setCreated(reader.nextString());

            } else if (key.equals("last_modified")) {
                builder.setLastModified(reader.nextString());

            } else if (key.equals("privacy_level")) {
                builder.setPrivacyLevel(PrivacyLevel.fromName(reader.nextString()));

            } else if (key.equals("filehash")) {
                builder.setFileHash(reader.nextString());

            } else if (key.equals("document_id")) {
                builder.setDocumentId(reader.nextString());
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return builder.build();
    }

    private static int parseColor(JsonReader reader) throws JSONException, IOException {
        reader.beginObject();

        int r = 0;
        int g = 0;
        int b = 0;

        while (reader.hasNext()) {
            final String key = reader.nextName();

            if (key.equals("r")) {
                r = reader.nextInt();
            } else if (key.equals("g")) {
                g = reader.nextInt();
            } else if (key.equals("b")) {
                b = reader.nextInt();
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return Color.rgb(r, g, b);
    }

    private static List<Annotation.Position> parseBoundingPositions(JsonReader reader) throws JSONException, IOException {
        final List<Annotation.Position> positions = new ArrayList<Annotation.Position>();

        reader.beginArray();
        while (reader.hasNext()) {
            positions.add(parsePosition(reader));
        }
        reader.endArray();

        return positions;
    }

    private static Annotation.Position parsePosition(JsonReader reader) throws JSONException, IOException {
        Point topLeft = null;
        Point bottomRight = null;
        Integer page = null;

        reader.beginObject();

        while (reader.hasNext()) {
            final String key = reader.nextName();

            if (key.equals("page")) {
                page = reader.nextInt();
            } else if (key.equals("top_left")) {
                topLeft = parsePoint(reader);
            } else if (key.equals("bottom_right")) {
                bottomRight = parsePoint(reader);
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return new Annotation.Position(topLeft, bottomRight, page);
    }

    private static Point parsePoint(JsonReader reader) throws IOException {
        double x = 0;
        double y = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            final String key = reader.nextName();

            if (key.equals("x")) {
                x = reader.nextDouble();
            } else if (key.equals("y")) {
                y = reader.nextDouble();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Point(x, y);
    }

    /**
	 * Creating a Profile object from a json string
	 * 
	 * @param reader
	 * @return the Profile object
	 * @throws JSONException
	 */
    public static Profile parseProfile(JsonReader reader) throws JSONException, IOException {
		final Profile.Builder builder = new Profile.Builder();

        reader.beginObject();

		while (reader.hasNext()){
		  
			final String key = reader.nextName();
            if (key.equals("id")) {
                builder.setId(reader.nextString());

            } else if (key.equals("display_name")) {
                builder.setDisplayName(reader.nextString());

            } else if (key.equals("user_type")) {
                builder.setUserType(reader.nextString());

            } else if (key.equals("url")) {
                builder.setUrl(reader.nextString());

            } else if (key.equals("email")) {
                builder.setEmail(reader.nextString());

            } else if (key.equals("link")) {
                builder.setLink(reader.nextString());

            } else if (key.equals("first_name")) {
                builder.setFirstName(reader.nextString());

            } else if (key.equals("last_name")) {
                builder.setLastName(reader.nextString());

            } else if (key.equals("research_interests")) {
                builder.setResearchInterests(reader.nextString());

            } else if (key.equals("academic_status")) {
                builder.setAcademicStatus(reader.nextString());

            } else if (key.equals("verified")) {
                builder.setVerified(reader.nextBoolean());

            } else if (key.equals("created_at")) {
                builder.setCreatedAt(reader.nextString());

            } else if (key.equals("discipline")) {
                builder.setDiscipline(parseDiscipline(reader));

            } else if (key.equals("photo")) {
                builder.setPhoto(parsePhoto(reader));

            } else if (key.equals("education")) {
                builder.setEducation(parseEducationList(reader));

            } else if (key.equals("employment")) {
                builder.setEmployment(paserEmploymentList(reader));

            } else {
                reader.skipValue();
            }
		}

        reader.endObject();
		
		return builder.build();
	}

    private static Discipline parseDiscipline(JsonReader reader) throws IOException {
        final Discipline discipline = new Discipline();
        reader.beginObject();

        while (reader.hasNext()) {
            final String key = reader.nextName();
            if ("name".equals(key)) {
                discipline.name = reader.nextString();
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return discipline;
    }

    public static Photo parsePhoto(JsonReader reader) throws IOException {
        reader.beginObject();

        String original = null;
        String standard = null;
        String square = null;

        while (reader.hasNext()) {
            final String key = reader.nextName();
            if ("original".equals(key)) {
                original = reader.nextString();
            } else if ("standard".equals(key)) {
                standard = reader.nextString();
            } else if ("square".equals(key)) {
                square = reader.nextString();
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return new Photo(original, standard, square);
    }

    private static List<Employment> paserEmploymentList(JsonReader reader) throws IOException, JSONException {
        final List<Employment> list = new LinkedList<>();
        reader.beginArray();

        while (reader.hasNext()) {
            list.add(parseEmployment(reader));
        }

        reader.endArray();
        return list;
    }


    private static Employment parseEmployment(JsonReader reader) throws JSONException, IOException {
        final Employment.Builder builder = new Employment.Builder();
        reader.beginObject();

        while (reader.hasNext()) {

            final String key = reader.nextName();

            if (key.equals("id")) {
                builder.setId(reader.nextString());

            } else if (key.equals("institution")) {
                builder.setInstitution(reader.nextString());

            } else if (key.equals("position")) {
                builder.setPosition(reader.nextString());

            } else if (key.equals("start_date")) {
                builder.setStartDate(reader.nextString());

            } else if (key.equals("end_date")) {
                builder.setEndDate(reader.nextString());

            } else if (key.equals("website")) {
                builder.setWebsite(reader.nextString());

            } else if (key.equals("classes")) {
                builder.setClasses(parseStringList(reader));

            } else if (key.equals("is_main_employment")) {
                builder.setIsMainEmployment(reader.nextBoolean());

            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return builder.build();
    }


    private static List<Education> parseEducationList(JsonReader reader) throws IOException, JSONException {
        final List<Education> list = new LinkedList<>();
        reader.beginArray();

        while (reader.hasNext()) {
            list.add(parseEducation(reader));
        }

        reader.endArray();
        return list;
    }

    private static Education parseEducation(JsonReader reader) throws JSONException, IOException {
        final Education.Builder builder = new Education.Builder();

        reader.beginObject();

        while (reader.hasNext()) {
            final String key = reader.nextName();
            if (key.equals("id")) {
                builder.setId(reader.nextString());
            } else if (key.equals("degree")) {
                builder.setDegree(reader.nextString());
            } else if (key.equals("institution")) {
                builder.setInstitution(reader.nextString());
            } else if (key.equals("start_date")) {
                builder.setStartDate(reader.nextString());
            } else if (key.equals("end_date")) {
                builder.setEndDate(reader.nextString());
            } else if (key.equals("website")) {
                builder.setWebsite(reader.nextString());
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return builder.build();
    }

    /**
     * Creating a Group object from a json string
     *
     * @param reader
     * @return the Group object
     * @throws JSONException
     */
    public static Group parseGroup(JsonReader reader) throws JSONException, IOException {
        final Group.Builder builder = new Group.Builder();
        reader.beginObject();

        while (reader.hasNext()) {

            final String key = reader.nextName();

            if (key.equals("id")) {
                builder.setId(reader.nextString());

            } else if (key.equals("created")) {
                builder.setCreated(reader.nextString());

            } else if (key.equals("owning_profile_id")) {
                builder.setOwningProfileId(reader.nextString());

            } else if (key.equals("link")) {
                builder.setLink(reader.nextString());

            } else if (key.equals("role")) {
                builder.setRole(Group.Role.fromValue(reader.nextString()));

            } else if (key.equals("access_level")) {
                builder.setAccessLevel(Group.AccessLevel.fromValue(reader.nextString()));

            } else if (key.equals("name")) {
                builder.setName(reader.nextString());

            } else if (key.equals("description")) {
                builder.setDescription(reader.nextString());

            } else if (key.equals("tags")) {
                builder.setTags(parseStringList(reader));

            } else if (key.equals("webpage")) {
                builder.setWebpage(reader.nextString());

            } else if (key.equals("disciplines")) {
                builder.setDisciplines(parseStringList(reader));

            } else if (key.equals("photo")) {
                builder.setPhoto(parsePhoto(reader));

            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return builder.build();
    }

	/**
	 * Creating a Document object from a json string
	 * 
	 * @param reader
	 * @return the Document object
	 * @throws JSONException
	 */
    public static Document parseDocument(JsonReader reader) throws JSONException, IOException {

		final Document.Builder bld = new Document.Builder();

        reader.beginObject();
		while (reader.hasNext()) {

			final String key = reader.nextName();
            if (key.equals("title")) {
                bld.setTitle(reader.nextString());

            } else if (key.equals("type")) {
                bld.setType(reader.nextString());

            } else if (key.equals("last_modified")) {
                bld.setLastModified(reader.nextString());

            } else if (key.equals("group_id")) {
                bld.setGroupId(reader.nextString());

            } else if (key.equals("profile_id")) {
                bld.setProfileId(reader.nextString());

            } else if (key.equals("read")) {
                bld.setRead(reader.nextBoolean());

            } else if (key.equals("starred")) {
                bld.setStarred(reader.nextBoolean());

            } else if (key.equals("authored")) {
                bld.setAuthored(reader.nextBoolean());

            } else if (key.equals("confirmed")) {
                bld.setConfirmed(reader.nextBoolean());

            } else if (key.equals("hidden")) {
                bld.setHidden(reader.nextBoolean());

            } else if (key.equals("id")) {
                bld.setId(reader.nextString());

            } else if (key.equals("month")) {
                bld.setMonth(reader.nextInt());

            } else if (key.equals("year")) {
                bld.setYear(reader.nextInt());

            } else if (key.equals("day")) {
                bld.setDay(reader.nextInt());

            } else if (key.equals("source")) {
                bld.setSource(reader.nextString());

            } else if (key.equals("revision")) {
                bld.setRevision(reader.nextString());

            } else if (key.equals("created")) {
                bld.setCreated(reader.nextString());

            } else if (key.equals("abstract")) {
                bld.setAbstractString(reader.nextString());

            } else if (key.equals("pages")) {
                bld.setPages(reader.nextString());

            } else if (key.equals("volume")) {
                bld.setVolume(reader.nextString());

            } else if (key.equals("issue")) {
                bld.setIssue(reader.nextString());

            } else if (key.equals("publisher")) {
                bld.setPublisher(reader.nextString());

            } else if (key.equals("city")) {
                bld.setCity(reader.nextString());

            } else if (key.equals("edition")) {
                bld.setEdition(reader.nextString());

            } else if (key.equals("institution")) {
                bld.setInstitution(reader.nextString());

            } else if (key.equals("series")) {
                bld.setSeries(reader.nextString());

            } else if (key.equals("chapter")) {
                bld.setChapter(reader.nextString());

            } else if (key.equals("client_data")) {
                bld.setClientData(reader.nextString());

            } else if (key.equals("unique_id")) {
                bld.setUniqueId(reader.nextString());

            } else if (key.equals("authors")) {
                bld.setAuthors(parsePersons(reader));

            } else if (key.equals("editors")) {
                bld.setEditors(parsePersons(reader));

            } else if (key.equals("identifiers")) {
                bld.setIdentifiers(parseStringMap(reader));
            } else if (key.equals("tags")) {
                bld.setTags(parseStringList(reader));

            } else if (key.equals("accessed")) {
                bld.setAccessed(reader.nextString());

            } else if (key.equals("file_attached")) {
                bld.setFileAttached(reader.nextBoolean());

            } else if (key.equals("keywords")) {
                bld.setKeywords(parseStringList(reader));

            } else if (key.equals("websites")) {
                bld.setWebsites(parseStringList(reader));
            } else {
                reader.skipValue();
            }
		}

        reader.endObject();

		return bld.build();
	}



    private static List<String> parseStringList(JsonReader reader) throws IOException {
        List<String> list = new LinkedList<String>();

        reader.beginArray();
        while (reader.hasNext()) {
            list.add(reader.nextString());
        }
        reader.endArray();
        return list;
    }

    private static Map<String, String> parseStringMap(JsonReader reader) throws IOException {
        Map<String, String> map = new HashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {
            map.put(reader.nextName(), reader.nextString());
        }
        reader.endObject();
        return map;
    }

    public static ArrayList<Person> parsePersons(JsonReader reader) throws JSONException, IOException {
        final ArrayList<Person> authorsList = new ArrayList<Person>();

        reader.beginArray();
        while (reader.hasNext()) {
            final Person author = parsePerson(reader);
            authorsList.add(author);
        }
        reader.endArray();
        return authorsList;
    }

    private static Person parsePerson(JsonReader reader) throws IOException {
        reader.beginObject();

        String authorName = null;
        String authorLastName = null;

        while (reader.hasNext()) {
            final String key = reader.nextName();
            if ("first_name".equals(key)) {
                authorName = reader.nextString();
            } else if ("last_name".equals(key)) {
                authorLastName = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Person(authorName, authorLastName);
    }

    /**
	 * Creating a list of File objects from a json string
	 * 
	 * @param reader
	 * @return the list of File objects
	 * @throws JSONException
	 */
    public static List<File> parseFileList(JsonReader reader) throws JSONException, IOException {
		
		final List<File> files = new ArrayList<File>();
		
		reader.beginArray();
		
		while (reader.hasNext()) {
			files.add(parseFile(reader));
		}

        reader.endArray();

		return files;
	}
	
	/**
	 *  Creating a list of Document objects from a json string
	 * 
	 * @param reader
	 * @return the list of Document objects
	 * @throws JSONException
	 */
    public static List<Document> parseDocumentList(JsonReader reader) throws JSONException, IOException {
        final List<Document> documents = new ArrayList<Document>();
        reader.beginArray();

        while (reader.hasNext()) {
            documents.add(parseDocument(reader));
        }

        reader.endArray();
        return documents;
    }

    public static List<Annotation> parseAnnotationList(JsonReader reader) throws JSONException, IOException {
        final List<Annotation> annotations = new ArrayList<Annotation>();
        reader.beginArray();

        while (reader.hasNext()) {
            annotations.add(parseAnnotation(reader));
        }

        reader.endArray();
        return annotations;
    }

    /**
     *  Creating a list of UserRole objects from a json string
     *
     * @param reader
     * @return the list of UserRole objects
     * @throws JSONException
     */
    public static List<UserRole> parseUserRoleList(JsonReader reader) throws JSONException, IOException {
        final List<UserRole> userRoles = new ArrayList<UserRole>();
        reader.beginArray();

        while (reader.hasNext()) {
            userRoles.add(parseUserRole(reader));
        }

        reader.endArray();
        return userRoles;
    }

    /**
     *  Creating a list of Group objects from a json string
     *
     * @param reader
     * @return the list of Group objects
     * @throws JSONException
     */
    public static List<Group> parseGroupList(JsonReader reader) throws JSONException, IOException {
        final List<Group> groups = new ArrayList<Group>();
        reader.beginArray();

        while (reader.hasNext()) {
            groups.add(parseGroup(reader));
        }

        reader.endArray();
        return groups;
    }

    /**
     * Creating a list of {@link ReadPosition} from a JSON string
     *
     * @param reader
     * @return the list of objects
     * @throws JSONException
     */
    public static List<ReadPosition> parseReadPositionList(JsonReader reader) throws JSONException, ParseException, IOException {
        final List<ReadPosition> readPositions = new LinkedList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            readPositions.add(parseReadPosition(reader));
        }
        reader.endArray();
        return readPositions;
    }

    /**
     * Creating a {@link ReadPosition} from a JSON string
     *
     * @param reader
     * @return the list of objects
     * @throws JSONException
     */
    public static ReadPosition parseReadPosition(JsonReader reader) throws JSONException, ParseException, IOException {
        final ReadPosition.Builder bld = new ReadPosition.Builder();
        reader.beginObject();

        while (reader.hasNext()) {
            final String key = reader.nextName();

            if (key.equals("id")) {
                bld.setId(reader.nextString());

            } else if (key.equals("file_id")) {
                bld.setFileId(reader.nextString());

            } else if (key.equals("page")) {
                bld.setPage(reader.nextInt());

            } else if (key.equals("vertical_position")) {
                bld.setVerticalPosition((float)reader.nextDouble());

            } else if (key.equals("date")) {
                bld.setDate(DateUtils.parseMendeleyApiTimestamp(reader.nextString()));
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
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

    public static List<String> parseApplicationFeatures(JsonReader reader) throws JSONException, IOException {
        final List<String> featureList = new LinkedList<String>();

        reader.beginArray();

        while (reader.hasNext()) {
            featureList.add(parseApplicationFeature(reader));
        }

        reader.endArray();
        return featureList;
    }

    private static String parseApplicationFeature(JsonReader reader) throws IOException {
        reader.beginObject();

        String featureName = null;

        while (reader.hasNext()) {
            final String key = reader.nextName();

            if (key.equals("name")) {
                featureName = reader.nextString();
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();
        return featureName;
    }
}
